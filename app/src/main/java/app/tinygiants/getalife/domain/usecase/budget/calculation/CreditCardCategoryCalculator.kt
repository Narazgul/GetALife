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
 * Calculator responsible for credit card payment category calculations.
 * Handles YNAB-style invisible money movement from credit card spending to payment categories.
 */
class CreditCardCategoryCalculator @Inject constructor(
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    /**
     * Calculates the spent amount for credit card payment categories.
     * This represents actual payments made to credit card accounts.
     */
    suspend fun calculateSpentAmount(
        categoryId: Long,
        yearMonth: YearMonth,
        allTransactions: List<Transaction>
    ): Money = withContext(defaultDispatcher) {

        val totalSpent = allTransactions
            .filter { transaction ->
                transaction.category?.id == categoryId &&
                        transaction.transactionDirection.name == "CreditCardPayment" &&
                        isTransactionInMonth(transaction.dateOfTransaction, yearMonth)
            }
            .sumOf { transaction -> kotlin.math.abs(transaction.amount.asDouble()) }

        Money(totalSpent)
    }

    /**
     * Calculates invisible inflow from credit card spending that should be "moved" to this payment category.
     * This implements YNAB-style invisible money movement where credit card spending creates
     * virtual income in the corresponding payment category.
     */
    suspend fun calculateInvisibleInflowFromCreditCardSpending(
        creditCardAccountId: Long,
        yearMonth: YearMonth,
        allTransactions: List<Transaction>
    ): Money = withContext(defaultDispatcher) {

        val creditCardSpending = allTransactions
            .filter { transaction ->
                transaction.account.id == creditCardAccountId &&
                        transaction.account.type == AccountType.CreditCard &&
                        transaction.transactionDirection.name == "Outflow" &&
                        isTransactionInMonth(transaction.dateOfTransaction, yearMonth)
            }
            .sumOf { transaction -> kotlin.math.abs(transaction.amount.asDouble()) }

        Money(creditCardSpending)
    }

    private fun isTransactionInMonth(instant: kotlin.time.Instant, yearMonth: YearMonth): Boolean {
        val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
        return localDateTime.year == yearMonth.year && localDateTime.month == yearMonth.month
    }
}