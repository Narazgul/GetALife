package app.tinygiants.getalife.domain.usecase.budget

import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.CategoryBehaviorType
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class RecalculateCategoryMonthlyStatusUseCase @Inject constructor(
    private val statusRepository: CategoryMonthlyStatusRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository
) {

    /**
     * Recalculates and persists pre-calculated fields for a specific category and month.
     * This includes YNAB-style invisible money movement for credit card spending.
     */
    suspend operator fun invoke(categoryId: Long, yearMonth: YearMonth) {
        val allTransactions = transactionRepository.getTransactionsFlow().first()
        val category = categoryRepository.getCategory(categoryId) ?: return

        // Get current status (if exists)
        val currentStatus = statusRepository.getStatus(categoryId, yearMonth)
        if (currentStatus == null) {
            // No status exists, nothing to recalculate
            return
        }

        when (category.behaviorType) {
            CategoryBehaviorType.Normal -> {
                // Regular category: calculate spent amount normally, but exclude credit card spending
                // which gets "moved" to payment categories invisibly
                val spentAmount = calculateSpentAmountForNormalCategory(categoryId, yearMonth, allTransactions)
                val carryOverFromPrevious = if (currentStatus.isCarryOverEnabled) {
                    calculateCarryOverFromPrevious(categoryId, yearMonth)
                } else {
                    EmptyMoney()
                }
                val availableAmount = carryOverFromPrevious + currentStatus.assignedAmount - spentAmount

                updateStatus(currentStatus, spentAmount, availableAmount, yearMonth, carryOverFromPrevious)
            }

            CategoryBehaviorType.CreditCardPayment -> {
                // Credit card payment category: receives invisible money from spending on credit card
                val spentAmount = calculateSpentAmountForPaymentCategory(categoryId, yearMonth, allTransactions)
                val invisibleInflowFromCreditCardSpending = calculateInvisibleInflowFromCreditCardSpending(
                    category.linkedAccountId!!, yearMonth, allTransactions
                )
                val carryOverFromPrevious = if (currentStatus.isCarryOverEnabled) {
                    calculateCarryOverFromPrevious(categoryId, yearMonth)
                } else {
                    EmptyMoney()
                }

                // Available = CarryOver + Assigned + Invisible inflow from credit card spending - Spent on payments
                val availableAmount = carryOverFromPrevious + currentStatus.assignedAmount +
                        invisibleInflowFromCreditCardSpending - spentAmount

                updateStatus(currentStatus, spentAmount, availableAmount, yearMonth, carryOverFromPrevious)
            }
        }
    }

    private suspend fun updateStatus(
        currentStatus: app.tinygiants.getalife.domain.model.CategoryMonthlyStatus,
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

    /**
     * Calculate invisible inflow from credit card spending that should be "moved" to this payment category
     */
    private fun calculateInvisibleInflowFromCreditCardSpending(
        creditCardAccountId: Long,
        yearMonth: YearMonth,
        allTransactions: List<Transaction>
    ): Money {
        val creditCardSpending = allTransactions
            .filter { transaction ->
                transaction.account.id == creditCardAccountId &&
                        transaction.account.type == AccountType.CreditCard &&
                        transaction.transactionDirection.name == "Outflow" &&
                        isTransactionInMonth(transaction.dateOfTransaction, yearMonth)
            }
            .sumOf { transaction -> kotlin.math.abs(transaction.amount.asDouble()) }

        return Money(creditCardSpending)
    }

    /**
     * Calculate spent amount for normal categories, excluding credit card spending
     * (since that money gets "moved" to payment categories)
     */
    private fun calculateSpentAmountForNormalCategory(
        categoryId: Long,
        yearMonth: YearMonth,
        allTransactions: List<Transaction>
    ): Money {
        val totalSpent = allTransactions
            .filter { transaction ->
                transaction.category?.id == categoryId &&
                        transaction.transactionDirection.name == "Outflow" &&
                        transaction.account.type != AccountType.CreditCard && // Exclude credit card spending
                        isTransactionInMonth(transaction.dateOfTransaction, yearMonth)
            }
            .sumOf { transaction -> kotlin.math.abs(transaction.amount.asDouble()) }

        return Money(totalSpent)
    }

    /**
     * Calculate spent amount for credit card payment categories (actual payments made)
     */
    private fun calculateSpentAmountForPaymentCategory(
        categoryId: Long,
        yearMonth: YearMonth,
        allTransactions: List<Transaction>
    ): Money {
        val totalSpent = allTransactions
            .filter { transaction ->
                transaction.category?.id == categoryId &&
                        transaction.transactionDirection.name == "CreditCardPayment" &&
                        isTransactionInMonth(transaction.dateOfTransaction, yearMonth)
            }
            .sumOf { transaction -> kotlin.math.abs(transaction.amount.asDouble()) }

        return Money(totalSpent)
    }

    private fun isTransactionInMonth(instant: kotlin.time.Instant, yearMonth: YearMonth): Boolean {
        val localDateTime = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        return localDateTime.year == yearMonth.year && localDateTime.month == yearMonth.month
    }

    private suspend fun calculateCarryOverFromPrevious(categoryId: Long, yearMonth: YearMonth): Money {
        val previousMonth = if (yearMonth.month.ordinal == 0) {
            YearMonth(yearMonth.year - 1, Month.DECEMBER)
        } else {
            YearMonth(yearMonth.year, Month.entries[yearMonth.month.ordinal - 1])
        }

        val previousStatus = statusRepository.getStatus(categoryId, previousMonth)
        return previousStatus?.availableAmount ?: EmptyMoney()
    }
}