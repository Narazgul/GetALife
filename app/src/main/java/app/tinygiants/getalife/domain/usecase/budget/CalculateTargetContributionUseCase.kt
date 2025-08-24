package app.tinygiants.getalife.domain.usecase.budget

import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TargetType
import kotlinx.datetime.number
import kotlinx.datetime.toJavaLocalDate
import javax.inject.Inject
import java.time.LocalDate as JavaLocalDate

/**
 * Calculates the monthly contribution needed to meet a category's target.
 *
 * For NEEDED_FOR_SPENDING: Returns the target amount (what's needed monthly)
 * For SAVINGS_BALANCE: Calculates monthly amount needed to reach target by date
 * For NONE: Returns null
 */
class CalculateTargetContributionUseCase @Inject constructor() {

    operator fun invoke(category: Category, availableAmount: Money = Money(0.0)): Money? {
        return when (category.targetType) {
            TargetType.NONE -> null

            TargetType.NEEDED_FOR_SPENDING -> {
                // For spending targets, return the target amount directly
                category.targetAmount
            }

            TargetType.SAVINGS_BALANCE -> {
                calculateSavingsContribution(category, availableAmount)
            }
        }
    }

    private fun calculateSavingsContribution(category: Category, availableAmount: Money): Money? {
        val targetAmount = category.targetAmount ?: return null
        val targetDate = category.targetDate ?: return null

        val todayDate = JavaLocalDate.now()

        // If target date is in the past or today, no monthly contribution needed
        if (targetDate.toJavaLocalDate() <= todayDate) {
            return Money(0.0)
        }

        // Calculate remaining amount to save
        val stillNeeded = targetAmount - availableAmount
        if (stillNeeded.asDouble() <= 0.0) {
            return Money(0.0)
        }

        // Calculate how many full months we need to save (month-by-month approach)
        val currentYear = todayDate.year
        val currentMonth = todayDate.monthValue
        val targetYear = targetDate.year
        val targetMonth = targetDate.month.number

        // Calculate the number of months from current month to target month (inclusive)
        val totalMonths = if (targetYear == currentYear) {
            // Same year: from current month to target month
            (targetMonth - currentMonth + 1).coerceAtLeast(1)
        } else {
            // Different years: months remaining in current year + full years + months in target year
            val monthsInCurrentYear = 12 - currentMonth + 1
            val fullYears = (targetYear - currentYear - 1) * 12
            val monthsInTargetYear = targetMonth
            monthsInCurrentYear + fullYears + monthsInTargetYear
        }

        // Distribute the remaining amount evenly across the months
        return Money(stillNeeded.asDouble() / totalMonths)
    }

    /**
     * Calculates the contribution needed specifically for a given month
     * This considers that money may have been allocated to the current month already
     */
    fun calculateForSpecificMonth(
        category: Category,
        availableAmount: Money = Money(0.0),
        currentMonth: Int,
        currentYear: Int
    ): Money? {
        if (category.targetType != TargetType.SAVINGS_BALANCE) {
            return invoke(category, availableAmount)
        }

        val targetAmount = category.targetAmount ?: return null
        val targetDate = category.targetDate ?: return null
        val todayDate = JavaLocalDate.now()

        // If target date is in the past or today, no monthly contribution needed
        if (targetDate.toJavaLocalDate() <= todayDate) {
            return Money(0.0)
        }

        // Calculate remaining amount to save
        val stillNeeded = targetAmount - availableAmount
        if (stillNeeded.asDouble() <= 0.0) {
            return Money(0.0)
        }

        // Calculate how many months from the REQUESTED month to target month
        val targetYear = targetDate.year
        val targetMonth = targetDate.month.number

        val totalMonths = if (targetYear == currentYear) {
            // Same year: from requested month to target month
            (targetMonth - currentMonth + 1).coerceAtLeast(1)
        } else {
            // Different years
            val monthsInCurrentYear = 12 - currentMonth + 1
            val fullYears = (targetYear - currentYear - 1) * 12
            val monthsInTargetYear = targetMonth
            monthsInCurrentYear + fullYears + monthsInTargetYear
        }

        // If this is the last month (target month), return the full remaining amount
        if (totalMonths == 1) {
            return stillNeeded
        }

        // Otherwise, distribute evenly across remaining months
        return Money(stillNeeded.asDouble() / totalMonths)
    }

    /**
     * Calculates what's needed for the current month specifically, based on equal monthly goals.
     * This maintains consistent monthly targets regardless of what's already been saved.
     */
    fun calculateCurrentMonthNeed(
        category: Category,
        availableAmount: Money = Money(0.0)
    ): Money? {
        if (category.targetType != TargetType.SAVINGS_BALANCE) {
            return invoke(category, availableAmount)
        }

        val targetAmount = category.targetAmount ?: return null
        val targetDate = category.targetDate ?: return null
        val todayDate = JavaLocalDate.now()

        // If target date is in the past or today, no monthly contribution needed
        if (targetDate.toJavaLocalDate() <= todayDate) {
            return Money(0.0)
        }

        // Calculate how many total months from the BEGINNING to target
        JavaLocalDate.of(todayDate.year, todayDate.monthValue, 1) // First day of current month
        val targetYear = targetDate.year
        val targetMonth = targetDate.month.number

        val totalMonthsInPlan = if (targetYear == todayDate.year) {
            // Same year: from current month to target month
            (targetMonth - todayDate.monthValue + 1).coerceAtLeast(1)
        } else {
            // Different years
            val monthsInCurrentYear = 12 - todayDate.monthValue + 1
            val fullYears = (targetYear - todayDate.year - 1) * 12
            val monthsInTargetYear = targetMonth
            monthsInCurrentYear + fullYears + monthsInTargetYear
        }

        // Calculate the equal monthly target amount
        val monthlyTarget = targetAmount.asDouble() / totalMonthsInPlan

        // For current month: monthly target minus what's already available
        val currentMonthNeed = monthlyTarget - availableAmount.asDouble()

        return Money(currentMonthNeed.coerceAtLeast(0.0))
    }

    /**
     * Gets the equal monthly target amount (what SHOULD be saved each month)
     */
    fun getMonthlyTargetAmount(category: Category): Money? {
        if (category.targetType != TargetType.SAVINGS_BALANCE) {
            return category.targetAmount
        }

        val targetAmount = category.targetAmount ?: return null
        val targetDate = category.targetDate ?: return null
        val todayDate = JavaLocalDate.now()

        // If target date is in the past, return zero
        if (targetDate.toJavaLocalDate() <= todayDate) {
            return Money(0.0)
        }

        // Calculate total months in the savings plan
        val targetYear = targetDate.year
        val targetMonth = targetDate.month.number

        val totalMonths = if (targetYear == todayDate.year) {
            (targetMonth - todayDate.monthValue + 1).coerceAtLeast(1)
        } else {
            val monthsInCurrentYear = 12 - todayDate.monthValue + 1
            val fullYears = (targetYear - todayDate.year - 1) * 12
            val monthsInTargetYear = targetMonth
            monthsInCurrentYear + fullYears + monthsInTargetYear
        }

        return Money(targetAmount.asDouble() / totalMonths)
    }
}