package app.tinygiants.getalife.domain.usecase.budget.calculation

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * Calculator responsible for computing spent amounts for normal categories.
 * Excludes credit card spending which gets "moved" to payment categories invisibly.
 */
class NormalCategoryCalculator @Inject constructor(
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    /**
     * Calculates the spent amount for a normal category, excluding credit card spending.
     * Credit card spending is handled separately as invisible money movement to payment categories.
     */
    suspend operator fun invoke(
        categoryId: Long,
        yearMonth: YearMonth,
        allTransactions: List<Transaction>
    ): Money = withContext(defaultDispatcher) {

        val totalSpent = allTransactions
            .filter { transaction ->
                transaction.category?.id == categoryId &&
                        transaction.transactionDirection.name == "Outflow" &&
                        transaction.account.type != AccountType.CreditCard && // Exclude credit card spending
                        isTransactionInMonth(transaction.dateOfTransaction, yearMonth)
            }
            .sumOf { transaction -> kotlin.math.abs(transaction.amount.asDouble()) }

        Money(totalSpent)
    }

    private fun isTransactionInMonth(instant: kotlin.time.Instant, yearMonth: YearMonth): Boolean {
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return localDateTime.year == yearMonth.year && localDateTime.month == yearMonth.month
    }
}