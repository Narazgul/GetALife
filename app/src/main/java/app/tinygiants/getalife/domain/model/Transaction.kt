package app.tinygiants.getalife.domain.model

import kotlin.time.Instant

data class Transaction(
    val id: Long,
    val amount: Money,
    val account: Account,
    val category: Category?,
    val transactionPartner: String,
    val transactionDirection: TransactionDirection,
    val description: String,
    val dateOfTransaction: Instant,
    val updatedAt: Instant,
    val createdAt: Instant,
    val isRecurring: Boolean = false,
    val recurrenceFrequency: RecurrenceFrequency? = null,
    val nextPaymentDate: Instant? = null,
    val recurrenceEndDate: Instant? = null,
    val isRecurrenceActive: Boolean = true,
    val parentRecurringTransactionId: Long? = null
) {
    val isRecurringTemplate: Boolean get() = isRecurring && parentRecurringTransactionId == null
    val isGeneratedFromRecurring: Boolean get() = parentRecurringTransactionId != null
}

enum class TransactionDirection { Unknown, Inflow, Outflow, AccountTransfer, CreditCardPayment }

enum class RecurrenceFrequency(val daysInterval: Int) {
    NEVER(0), // Default - not recurring
    DAILY(1),
    WEEKLY(7),
    EVERY_OTHER_WEEK(14),
    TWICE_A_MONTH(15), // Approximation - real implementation should use calendar
    MONTHLY(30),
    EVERY_4_WEEKS(28),
    EVERY_OTHER_MONTH(60),
    EVERY_3_MONTHS(90),
    EVERY_4_MONTHS(120),
    TWICE_A_YEAR(182), // Approximation - 365/2 ≈ 182
    YEARLY(365)
}