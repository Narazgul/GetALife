package app.tinygiants.getalife.domain.usecase.transaction.recurrence

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.RecurrenceFrequency
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * Use case responsible for calculating next payment dates for recurring transactions.
 * Handles all recurrence frequency calculations with proper calendar awareness.
 */
class CalculateRecurrenceDatesUseCase @Inject constructor(
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    /**
     * Calculates the next payment date based on current date and recurrence frequency.
     */
    suspend operator fun invoke(
        currentDate: kotlin.time.Instant,
        frequency: RecurrenceFrequency
    ): kotlin.time.Instant = withContext(defaultDispatcher) {
        calculateNextPaymentDate(currentDate, frequency)
    }

    private fun calculateNextPaymentDate(
        currentDate: kotlin.time.Instant,
        frequency: RecurrenceFrequency
    ): kotlin.time.Instant {
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
            RecurrenceFrequency.TWICE_A_MONTH -> calculateTwiceMonthlyDate(localDate)
        }

        // Convert back to Instant with same time
        return nextLocalDate.atTime(time).toInstant(timeZone)
    }

    private fun calculateTwiceMonthlyDate(localDate: LocalDate): LocalDate {
        val dayOfMonth = localDate.day
        return if (dayOfMonth <= 15) {
            // Move to 15th of same month
            LocalDate(localDate.year, localDate.month, 15)
        } else {
            // Move to 1st of next month
            LocalDate(localDate.year, localDate.month, 1).plus(DatePeriod(months = 1))
        }
    }
}