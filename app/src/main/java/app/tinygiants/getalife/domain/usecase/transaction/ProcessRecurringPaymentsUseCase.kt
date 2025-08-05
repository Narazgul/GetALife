package app.tinygiants.getalife.domain.usecase.transaction

import app.tinygiants.getalife.domain.model.RecurrenceFrequency
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.repository.TransactionRepository
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant

class ProcessRecurringPaymentsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke() {
        val currentTime = Clock.System.now()
        val dueTransactions = transactionRepository.getDueRecurringTransactions(currentTime)

        dueTransactions.forEach { recurringTransaction ->
            // Create new transaction from recurring template
            val newTransaction = Transaction(
                id = 0, // Auto-generate new ID
                amount = recurringTransaction.amount,
                account = recurringTransaction.account,
                category = recurringTransaction.category,
                transactionPartner = recurringTransaction.transactionPartner,
                transactionDirection = recurringTransaction.transactionDirection,
                description = "${recurringTransaction.description} (Automatisch)",
                dateOfTransaction = currentTime,
                updatedAt = currentTime,
                createdAt = currentTime,
                isRecurring = false, // Generated transactions are not recurring themselves
                parentRecurringTransactionId = recurringTransaction.id
            )

            // Add the new transaction
            transactionRepository.addTransaction(newTransaction)

            // Update next payment date for recurring template
            recurringTransaction.nextPaymentDate?.let { nextDate ->
                recurringTransaction.recurrenceFrequency?.let { frequency ->
                    val newNextPaymentDate = calculateNextPaymentDate(nextDate, frequency)
                    transactionRepository.updateNextPaymentDate(recurringTransaction.id, newNextPaymentDate)
                }
            }
        }
    }

    private fun calculateNextPaymentDate(
        currentDate: Instant,
        frequency: RecurrenceFrequency
    ): Instant {
        val timeZone = TimeZone.currentSystemDefault()
        val localDateTime = currentDate.toLocalDateTime(timeZone)
        val localDate = localDateTime.date
        val time = localDateTime.time

        val nextLocalDate = when (frequency) {
            RecurrenceFrequency.NEVER -> localDate // Should not be called for NEVER, but return same date as fallback
            // Day-based frequencies
            RecurrenceFrequency.DAILY -> localDate.plus(DatePeriod(days = 1))
            RecurrenceFrequency.WEEKLY -> localDate.plus(DatePeriod(days = 7))
            RecurrenceFrequency.EVERY_OTHER_WEEK -> localDate.plus(DatePeriod(days = 14))
            RecurrenceFrequency.EVERY_4_WEEKS -> localDate.plus(DatePeriod(days = 28))

            // Month-based frequencies (calendar-aware)
            RecurrenceFrequency.MONTHLY -> localDate.plus(DatePeriod(months = 1))
            RecurrenceFrequency.EVERY_OTHER_MONTH -> localDate.plus(DatePeriod(months = 2))
            RecurrenceFrequency.EVERY_3_MONTHS -> localDate.plus(DatePeriod(months = 3))
            RecurrenceFrequency.EVERY_4_MONTHS -> localDate.plus(DatePeriod(months = 4))
            RecurrenceFrequency.TWICE_A_YEAR -> localDate.plus(DatePeriod(months = 6))
            RecurrenceFrequency.YEARLY -> localDate.plus(DatePeriod(years = 1))

            // Special case: twice a month
            RecurrenceFrequency.TWICE_A_MONTH -> {
                val dayOfMonth = localDate.day
                if (dayOfMonth <= 15) {
                    // Move to 15th of same month
                    LocalDate(localDate.year, localDate.month, 15)
                } else {
                    // Move to 1st of next month
                    LocalDate(localDate.year, localDate.month, 1).plus(DatePeriod(months = 1))
                }
            }
        }

        // Convert back to Instant with same time
        return nextLocalDate.atTime(time).toInstant(timeZone)
    }
}