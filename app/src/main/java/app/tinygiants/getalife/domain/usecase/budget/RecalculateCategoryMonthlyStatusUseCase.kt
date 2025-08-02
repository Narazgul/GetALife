package app.tinygiants.getalife.domain.usecase.budget

import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

class RecalculateCategoryMonthlyStatusUseCase @Inject constructor(
    private val statusRepository: CategoryMonthlyStatusRepository,
    private val transactionRepository: TransactionRepository
) {

    /**
     * Recalculates and persists pre-calculated fields for a specific category and month.
     * This optimizes future queries by avoiding runtime calculations.
     */
    suspend operator fun invoke(categoryId: Long, yearMonth: YearMonth) {
        val allTransactions = transactionRepository.getTransactionsFlow().first()

        // Get current status (if exists)
        val currentStatus = statusRepository.getStatus(categoryId, yearMonth)
        if (currentStatus == null) {
            // No status exists, nothing to recalculate
            return
        }

        // Calculate spent amount for this month
        val spentAmount = calculateSpentAmountFromTransactions(categoryId, yearMonth, allTransactions)

        // Calculate carry-over from previous month
        val carryOverFromPrevious = if (currentStatus.isCarryOverEnabled) {
            calculateCarryOverFromPrevious(categoryId, yearMonth)
        } else {
            EmptyMoney()
        }

        // Calculate available amount
        val availableAmount = carryOverFromPrevious + currentStatus.assignedAmount - spentAmount

        // Create updated status with pre-calculated values
        val updatedStatus = currentStatus.copy(
            spentAmount = spentAmount,
            availableAmount = availableAmount
        )

        // Persist to database with carry-over info
        statusRepository.saveStatus(updatedStatus, yearMonth, carryOverFromPrevious)
    }

    private suspend fun calculateCarryOverFromPrevious(categoryId: Long, yearMonth: YearMonth): Money {
        val previousMonth = if (yearMonth.month.ordinal == 0) {
            YearMonth(yearMonth.year - 1, kotlinx.datetime.Month.DECEMBER)
        } else {
            YearMonth(yearMonth.year, kotlinx.datetime.Month.entries[yearMonth.month.ordinal - 1])
        }

        val previousStatus = statusRepository.getStatus(categoryId, previousMonth)
        return previousStatus?.availableAmount ?: EmptyMoney()
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
            .sumOf { transaction -> kotlin.math.abs(transaction.amount.asDouble()) }

        return Money(totalSpent)
    }

    private fun isTransactionInMonth(instant: kotlin.time.Instant, yearMonth: YearMonth): Boolean {
        val localDateTime = instant.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        return localDateTime.year == yearMonth.year && localDateTime.month == yearMonth.month
    }
}