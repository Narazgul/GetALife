package app.tinygiants.getalife.domain.usecase.budget

import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.BudgetMonth
import app.tinygiants.getalife.domain.model.CategoryMonthlyStatus
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.includeInBudget
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.CalculateCategoryProgressUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.GetCategoriesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.datetime.YearMonth
import javax.inject.Inject

class GetBudgetForMonthUseCase @Inject constructor(
    private val statusRepository: CategoryMonthlyStatusRepository,
    private val groupRepository: GroupRepository,
    private val accountRepository: AccountRepository,
    private val getCategories: GetCategoriesUseCase,
    private val calculateCategoryProgress: CalculateCategoryProgressUseCase
) {
    operator fun invoke(yearMonth: YearMonth): Flow<Result<BudgetMonth>> {
        return combine(
            statusRepository.getStatusForMonthFlow(yearMonth),
            accountRepository.getAccountsFlow(),
            getCategories()
        ) { statusForMonth, accounts, allCategories ->
            try {
                calculateBudgetMonth(yearMonth, statusForMonth, accounts, allCategories)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun calculateBudgetMonth(
        yearMonth: YearMonth,
        statusForMonth: List<CategoryMonthlyStatus>,
        accounts: List<Account>,
        allCategories: List<app.tinygiants.getalife.domain.model.Category>
    ): Result<BudgetMonth> {
        // 1. Load groups for ordering and expansion state
        val groups = groupRepository.getGroupsFlow().first()

        // 2. Create status entries for all categories, using existing or creating default ones
        val statusMap = statusForMonth.associateBy { it.category.id }
        val allCategoryStatuses = allCategories.map { category ->
            statusMap[category.id] ?: CategoryMonthlyStatus(
                category = category,
                assignedAmount = EmptyMoney(),
                isCarryOverEnabled = true,
                spentAmount = EmptyMoney(),
                availableAmount = EmptyMoney(),
                progress = app.tinygiants.getalife.domain.model.EmptyProgress(),
                suggestedAmount = null
            )
        }

        // 3. Prepare map Group -> List<CategoryMonthlyStatus>
        val statusGrouped = allCategoryStatuses.groupBy { it.category.groupId }

        val groupsWithCategoryStatus: Map<Group, List<CategoryMonthlyStatus>> = groups.associateWith { group ->
            val statusesOfGroup = statusGrouped[group.id].orEmpty()
                .sortedBy { it.category.listPosition }

            statusesOfGroup.map { status ->
                // Use pre-calculated values from database for optimal performance
                val progress = calculateCategoryProgress(status)
                status.copy(progress = progress)
            }
        }

        // 4. Calculate total assigned money for this month only
        val totalAssignedMoney = statusForMonth.fold(EmptyMoney()) { acc, st ->
            acc + st.assignedAmount
        }

        // 5. Calculate total available money to assign (cross month)
        val totalAvailableMoneyToAssign = calculateTotalAvailableMoneyToAssign(accounts)

        val budgetMonth = BudgetMonth(
            yearMonth = yearMonth,
            totalAssignableMoney = totalAvailableMoneyToAssign,
            totalAssignedMoney = totalAssignedMoney,
            groups = groupsWithCategoryStatus
        )

        return Result.success(budgetMonth)
    }

    private suspend fun calculateTotalAvailableMoneyToAssign(accounts: List<Account>): Money {
        // Only include accounts that are in budget and not closed
        val totalAccountBalance = accounts
            .filter { it.type.includeInBudget && !it.isClosed }
            .sumOf { account ->
                when (account.type) {
                    AccountType.CreditCard -> {
                        // For credit cards: only include positive balances (credit/overpayment)
                        // Negative balances (debt) should not reduce assignable money
                        if (account.balance.asDouble() > 0) {
                            account.balance.asDouble()
                        } else {
                            0.0 // Don't count debt against assignable money
                        }
                    }

                    else -> {
                        // For all other account types, include the full balance
                        account.balance.asDouble()
                    }
                }
            }

        val allMonthlyStatuses = statusRepository.getAllStatuses()
        val totalAssignedMoneyAllMonths = allMonthlyStatuses.fold(EmptyMoney()) { acc: Money, status: CategoryMonthlyStatus ->
            acc + status.assignedAmount
        }

        return Money(totalAccountBalance) - totalAssignedMoneyAllMonths
    }
}