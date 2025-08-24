package app.tinygiants.getalife.domain.usecase.budget

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.CategoryBehaviorType
import app.tinygiants.getalife.domain.model.CategoryMonthlyStatus
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.usecase.budget.calculation.CarryOverCalculator
import app.tinygiants.getalife.domain.usecase.budget.calculation.CreditCardCategoryCalculator
import app.tinygiants.getalife.domain.usecase.budget.calculation.NormalCategoryCalculator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.datetime.YearMonth
import javax.inject.Inject

/**
 * Orchestrates the recalculation of category monthly status using specialized calculators.
 * Coordinates normal categories and credit card payment categories with their respective logic.
 */
class RecalculateCategoryMonthlyStatusUseCase @Inject constructor(
    private val statusRepository: CategoryMonthlyStatusRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val normalCategoryCalculator: NormalCategoryCalculator,
    private val creditCardCategoryCalculator: CreditCardCategoryCalculator,
    private val carryOverCalculator: CarryOverCalculator,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    /**
     * Recalculates and persists pre-calculated fields for a specific category and month.
     * This includes YNAB-style invisible money movement for credit card spending.
     */
    suspend operator fun invoke(categoryId: Long, yearMonth: YearMonth): Unit = withContext(defaultDispatcher) {
        val allTransactions = transactionRepository.getTransactionsFlow().first()
        val category = categoryRepository.getCategory(categoryId) ?: return@withContext

        // Get current status (if exists)
        val currentStatus = statusRepository.getStatus(categoryId, yearMonth) ?: return@withContext

        when (category.behaviorType) {
            CategoryBehaviorType.Normal -> {
                handleNormalCategory(currentStatus, categoryId, yearMonth, allTransactions)
            }

            CategoryBehaviorType.CreditCardPayment -> {
                handleCreditCardPaymentCategory(currentStatus, categoryId, yearMonth, allTransactions, category.linkedAccountId!!)
            }
        }
    }

    private suspend fun handleNormalCategory(
        currentStatus: CategoryMonthlyStatus,
        categoryId: Long,
        yearMonth: YearMonth,
        allTransactions: List<app.tinygiants.getalife.domain.model.Transaction>
    ) {
        val spentAmount = normalCategoryCalculator(categoryId, yearMonth, allTransactions)
        val carryOverFromPrevious = if (currentStatus.isCarryOverEnabled) {
            carryOverCalculator(categoryId, yearMonth)
        } else {
            EmptyMoney()
        }
        val availableAmount = carryOverFromPrevious + currentStatus.assignedAmount - spentAmount

        updateStatus(currentStatus, spentAmount, availableAmount, yearMonth, carryOverFromPrevious)
    }

    private suspend fun handleCreditCardPaymentCategory(
        currentStatus: CategoryMonthlyStatus,
        categoryId: Long,
        yearMonth: YearMonth,
        allTransactions: List<app.tinygiants.getalife.domain.model.Transaction>,
        creditCardAccountId: Long
    ) {
        val spentAmount = creditCardCategoryCalculator.calculateSpentAmount(categoryId, yearMonth, allTransactions)
        val invisibleInflowFromCreditCardSpending = creditCardCategoryCalculator
            .calculateInvisibleInflowFromCreditCardSpending(creditCardAccountId, yearMonth, allTransactions)

        val carryOverFromPrevious = if (currentStatus.isCarryOverEnabled) {
            carryOverCalculator(categoryId, yearMonth)
        } else {
            EmptyMoney()
        }

        // YNAB-style calculation:
        // Available = CarryOver + Assigned + Invisible inflow from credit card spending - Spent on payments
        val availableAmount = carryOverFromPrevious + currentStatus.assignedAmount +
                invisibleInflowFromCreditCardSpending - spentAmount

        updateStatus(currentStatus, spentAmount, availableAmount, yearMonth, carryOverFromPrevious)
    }

    private suspend fun updateStatus(
        currentStatus: CategoryMonthlyStatus,
        spentAmount: Money,
        availableAmount: Money,
        yearMonth: YearMonth,
        carryOverFromPrevious: Money
    ) {
        val updatedStatus = currentStatus.copy(
            spentAmount = spentAmount,
            availableAmount = availableAmount
        )
        statusRepository.saveStatus(updatedStatus, yearMonth, carryOverFromPrevious)
    }
}