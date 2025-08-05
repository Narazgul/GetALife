package app.tinygiants.getalife.domain.model

import app.tinygiants.getalife.R

/**
 * Extension function to get string resource for RecurrenceFrequency
 */
fun RecurrenceFrequency.asStringRes(): Int = when (this) {
    RecurrenceFrequency.NEVER -> R.string.recurrence_never
    RecurrenceFrequency.DAILY -> R.string.recurrence_daily
    RecurrenceFrequency.WEEKLY -> R.string.recurrence_weekly
    RecurrenceFrequency.EVERY_OTHER_WEEK -> R.string.recurrence_every_other_week
    RecurrenceFrequency.MONTHLY -> R.string.recurrence_monthly
    RecurrenceFrequency.TWICE_A_MONTH -> R.string.recurrence_twice_a_month
    RecurrenceFrequency.EVERY_OTHER_MONTH -> R.string.recurrence_every_other_month
    RecurrenceFrequency.EVERY_4_WEEKS -> R.string.recurrence_every_4_weeks
    RecurrenceFrequency.EVERY_3_MONTHS -> R.string.recurrence_every_3_months
    RecurrenceFrequency.EVERY_4_MONTHS -> R.string.recurrence_every_4_months
    RecurrenceFrequency.TWICE_A_YEAR -> R.string.recurrence_twice_a_year
    RecurrenceFrequency.YEARLY -> R.string.recurrence_yearly
}