package app.tinygiants.getalife.domain.usecase.budget

import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.BudgetMonth
import app.tinygiants.getalife.domain.model.CategoryMonthlyStatus
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.CalculateCategoryProgressUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class GetBudgetForMonthUseCase @Inject constructor(
    private val statusRepository: CategoryMonthlyStatusRepository,
    private val groupRepository: GroupRepository,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val calculateCategoryProgress: CalculateCategoryProgressUseCase
) {
    operator fun invoke(yearMonth: YearMonth): Flow<Result<BudgetMonth>> {
        return combine(
            statusRepository.getStatusForMonthFlow(yearMonth).distinctUntilChanged(),
            transactionRepository.getTransactionsFlow().distinctUntilChanged(),
            accountRepository.getAccountsFlow().distinctUntilChanged()
        ) { statusForMonth, allTransactions, accounts ->
            try {
                calculateBudgetMonth(yearMonth, statusForMonth, allTransactions, accounts)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun calculateBudgetMonth(
        yearMonth: YearMonth,
        statusForMonth: List<CategoryMonthlyStatus>,
        allTransactions: List<Transaction>,
        accounts: List<Account>
    ): Result<BudgetMonth> {
        // 1. Load groups for ordering and expansion state
        val groups = groupRepository.getGroupsFlow().first()

        // 2. Prepare map Group -> List<CategoryMonthlyStatus>
        val statusGrouped = statusForMonth.groupBy { it.category.groupId }

        val groupsWithCategoryStatus: Map<Group, List<CategoryMonthlyStatus>> = groups.associateWith { group ->
            val statusesOfGroup = statusGrouped[group.id].orEmpty()
                .sortedBy { it.category.listPosition }

            statusesOfGroup.map { status ->
                val spentAmount = calculateSpentAmountFromTransactions(status.category.id, yearMonth, allTransactions)
                val availableAmount = status.assignedAmount - spentAmount

                val updatedStatus = status.copy(
                    spentAmount = spentAmount,
                    availableAmount = availableAmount
                )

                val progress = calculateCategoryProgress(updatedStatus)
                updatedStatus.copy(progress = progress)
            }
        }

        // 3. Calculate total assigned money for this month only
        val totalAssignedMoney = statusForMonth.fold(EmptyMoney()) { acc, st ->
            acc + st.assignedAmount
        }

        // 4. Calculate total available money to assign (cross month)
        val totalAvailableMoneyToAssign = calculateTotalAvailableMoneyToAssign(accounts)

        val budgetMonth = BudgetMonth(
            yearMonth = yearMonth,
            totalAssignableMoney = totalAvailableMoneyToAssign,
            totalAssignedMoney = totalAssignedMoney,
            groups = groupsWithCategoryStatus
        )

        return Result.success(budgetMonth)
    }

    private fun calculateSpentAmountFromTransactions(
        categoryId: Long,
        yearMonth: YearMonth,
        allTransactions: List<Transaction>
    ): Money {
        val totalSpent = allTransactions
            .filter { transaction ->
                transaction.category?.id == categoryId &&
                        transaction.transactionDirection.name == "Outflow" &&
                        isTransactionInMonth(transaction.dateOfTransaction, yearMonth)
            }
            .sumOf { transaction -> transaction.amount.asDouble() }

        return Money(totalSpent)
    }

    private fun isTransactionInMonth(instant: kotlin.time.Instant, yearMonth: YearMonth): Boolean {
        val localDateTime = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        return localDateTime.year == yearMonth.year && localDateTime.month == yearMonth.month
    }

    private suspend fun calculateTotalAvailableMoneyToAssign(accounts: List<Account>): Money {
        val totalAccountBalance = accounts.fold(EmptyMoney()) { acc, account ->
            acc + account.balance
        }

        val allMonthlyStatuses = statusRepository.getAllStatuses()
        val totalAssignedMoneyAllMonths = allMonthlyStatuses.fold(EmptyMoney()) { acc: Money, status: CategoryMonthlyStatus ->
            acc + status.assignedAmount
        }

        return totalAccountBalance - totalAssignedMoneyAllMonths
    }
}