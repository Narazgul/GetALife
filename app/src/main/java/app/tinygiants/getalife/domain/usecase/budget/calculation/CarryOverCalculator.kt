package app.tinygiants.getalife.domain.usecase.budget.calculation

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Month
import kotlinx.datetime.YearMonth
import javax.inject.Inject

/**
 * Calculator responsible for computing carry-over amounts from previous months.
 * Handles month transitions including year boundaries.
 */
class CarryOverCalculator @Inject constructor(
    private val statusRepository: CategoryMonthlyStatusRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    /**
     * Calculates the carry-over amount from the previous month for a given category.
     * Returns EmptyMoney if no previous status exists.
     */
    suspend operator fun invoke(categoryId: Long, yearMonth: YearMonth): Money = withContext(defaultDispatcher) {
        val previousMonth = calculatePreviousMonth(yearMonth)
        val previousStatus = statusRepository.getStatus(categoryId, previousMonth)
        previousStatus?.availableAmount ?: EmptyMoney()
    }

    private fun calculatePreviousMonth(yearMonth: YearMonth): YearMonth {
        return if (yearMonth.month.ordinal == 0) {
            YearMonth(yearMonth.year - 1, Month.DECEMBER)
        } else {
            YearMonth(yearMonth.year, Month.entries[yearMonth.month.ordinal - 1])
        }
    }
}