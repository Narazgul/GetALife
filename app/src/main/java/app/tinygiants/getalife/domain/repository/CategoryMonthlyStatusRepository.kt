package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.domain.model.CategoryMonthlyStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.YearMonth

/**
 * Repository for category monthly status.
 * Only handles persistent data - calculations are done in use cases.
 */
interface CategoryMonthlyStatusRepository {
    suspend fun getStatus(categoryId: Long, yearMonth: YearMonth): CategoryMonthlyStatus?
    suspend fun getStatusForMonth(yearMonth: YearMonth): List<CategoryMonthlyStatus>
    suspend fun getAllStatuses(): List<CategoryMonthlyStatus>
    suspend fun saveStatus(status: CategoryMonthlyStatus, yearMonth: YearMonth)
    suspend fun deleteStatus(categoryId: Long, yearMonth: YearMonth)
    fun getStatusForMonthFlow(yearMonth: YearMonth): Flow<List<CategoryMonthlyStatus>>
}