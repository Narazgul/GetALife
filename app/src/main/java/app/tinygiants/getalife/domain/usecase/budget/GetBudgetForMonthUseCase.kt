package app.tinygiants.getalife.domain.usecase.budget

import app.tinygiants.getalife.domain.model.BudgetMonth
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.CategoryMonthlyStatus
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.AccountRepository
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.CalculateCategoryProgressUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.YearMonth
import javax.inject.Inject

class GetBudgetForMonthUseCase @Inject constructor(
    private val statusRepository: CategoryMonthlyStatusRepository,
    private val groupRepository: GroupRepository,
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val calculateCategoryProgress: CalculateCategoryProgressUseCase
) {
    operator fun invoke(yearMonth: YearMonth): Flow<Result<BudgetMonth>> = flow {
        try {
            // 1. Load raw data from repositories
            val groupsWithCategories = groupRepository.getGroupsWithCategories()
            val existingStatusForMonth = statusRepository.getStatusForMonth(yearMonth)
            val statusLookup = existingStatusForMonth.associateBy { it.category.id }

            val groupsWithCategoryStatus = mutableMapOf<Group, List<CategoryMonthlyStatus>>()
            var totalAssignedMoney = EmptyMoney()

            // 2. Calculate status for each category
            groupsWithCategories.forEach { (group: Group, categories: List<Category>) ->
                val categoryStatuses = categories.map { category ->
                    val existingStatus = statusLookup[category.id]

                    // All calculations happen here in the use case
                    val spentAmount = transactionRepository.getSpentAmountByCategoryAndMonth(category.id, yearMonth)
                    val assignedAmount = existingStatus?.assignedAmount ?: EmptyMoney()
                    val availableAmount = assignedAmount - spentAmount
                    val suggestedAmount = getSuggestedAmountForCategory(category)

                    // Create or update the status object BEFORE calculating progress
                    val currentStatus = existingStatus?.copy(
                        spentAmount = spentAmount,
                        availableAmount = availableAmount,
                        suggestedAmount = suggestedAmount
                    )
                        ?: // Create new status for category without assignment
                        CategoryMonthlyStatus(
                            category = category,
                            assignedAmount = EmptyMoney(),
                            isCarryOverEnabled = true,
                            spentAmount = spentAmount,
                            availableAmount = availableAmount,
                            progress = EmptyProgress(),
                            suggestedAmount = suggestedAmount
                        )

                    // Now calculate progress with the guaranteed non-null status
                    val progress = calculateCategoryProgress(currentStatus)

                    totalAssignedMoney = totalAssignedMoney + assignedAmount

                    // Return the final status with calculated progress
                    currentStatus.copy(progress = progress)
                }
                groupsWithCategoryStatus[group] = categoryStatuses
            }

            // 3. Calculate total available money to assign
            val totalAvailableMoneyToAssign = calculateTotalAvailableMoneyToAssign(totalAssignedMoney)

            val budgetMonth = BudgetMonth(
                yearMonth = yearMonth,
                totalAssignableMoney = totalAvailableMoneyToAssign,
                totalAssignedMoney = totalAssignedMoney,
                groups = groupsWithCategoryStatus
            )

            emit(Result.success(budgetMonth))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    private fun getSuggestedAmountForCategory(category: Category): Money? {
        return category.monthlyTargetAmount
    }

    private suspend fun calculateTotalAvailableMoneyToAssign(totalAssignedMoney: Money): Money {
        val allAccounts = accountRepository.getAccountsFlow().first()
        val totalAccountBalance = allAccounts.fold(EmptyMoney()) { acc, account ->
            acc + account.balance
        }

        return totalAccountBalance - totalAssignedMoney
    }
}