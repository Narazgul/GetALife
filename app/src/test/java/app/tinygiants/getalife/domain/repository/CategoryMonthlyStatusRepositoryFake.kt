package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.domain.model.CategoryMonthlyStatus
import app.tinygiants.getalife.domain.model.Money
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.YearMonth

class CategoryMonthlyStatusRepositoryFake : CategoryMonthlyStatusRepository {

    private val statuses = MutableStateFlow<Map<Pair<Long, YearMonth>, CategoryMonthlyStatus>>(emptyMap())

    override suspend fun getStatus(categoryId: Long, yearMonth: YearMonth): CategoryMonthlyStatus? {
        return statuses.value[categoryId to yearMonth]
    }

    override suspend fun getStatusForMonth(yearMonth: YearMonth): List<CategoryMonthlyStatus> {
        return statuses.value.values.filter { it.category.id to yearMonth in statuses.value.keys }
    }

    override suspend fun getAllStatuses(): List<CategoryMonthlyStatus> {
        return statuses.value.values.toList()
    }

    override suspend fun saveStatus(status: CategoryMonthlyStatus, yearMonth: YearMonth) {
        statuses.value = statuses.value.toMutableMap().apply {
            put(status.category.id to yearMonth, status)
        }
    }

    override suspend fun saveStatus(status: CategoryMonthlyStatus, yearMonth: YearMonth, carryOverFromPrevious: Money) {
        saveStatus(status, yearMonth)
    }

    override suspend fun deleteStatus(categoryId: Long, yearMonth: YearMonth) {
        statuses.value = statuses.value.toMutableMap().apply {
            remove(categoryId to yearMonth)
        }
    }

    override fun getStatusForMonthFlow(yearMonth: YearMonth): Flow<List<CategoryMonthlyStatus>> {
        return statuses.asStateFlow().map { statusMap ->
            statusMap.values.filter { status ->
                status.category.id to yearMonth in statusMap.keys
            }
        }
    }
}