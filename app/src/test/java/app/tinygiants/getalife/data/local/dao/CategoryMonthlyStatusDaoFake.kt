package app.tinygiants.getalife.data.local.dao

import app.tinygiants.getalife.data.local.entities.CategoryMonthlyStatusEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class CategoryMonthlyStatusDaoFake : CategoryMonthlyStatusDao {

    private val statusData = MutableStateFlow<List<CategoryMonthlyStatusEntity>>(emptyList())

    override suspend fun getStatusData(categoryId: Long, yearMonth: String): CategoryMonthlyStatusEntity? {
        return statusData.value.find { it.categoryId == categoryId && it.yearMonth == yearMonth }
    }

    override suspend fun getStatusDataForMonth(yearMonth: String): List<CategoryMonthlyStatusEntity> {
        return statusData.value.filter { it.yearMonth == yearMonth }
    }

    override fun getStatusDataForMonthFlow(yearMonth: String): Flow<List<CategoryMonthlyStatusEntity>> {
        return statusData.map { list -> list.filter { it.yearMonth == yearMonth } }
    }

    override suspend fun getAllStatusData(): List<CategoryMonthlyStatusEntity> {
        return statusData.value
    }

    override suspend fun insertOrUpdate(entity: CategoryMonthlyStatusEntity) {
        val updatedData = statusData.value.toMutableList()
        val existingIndex = updatedData.indexOfFirst { 
            it.categoryId == entity.categoryId && it.yearMonth == entity.yearMonth 
        }
        
        if (existingIndex != -1) {
            updatedData[existingIndex] = entity
        } else {
            updatedData.add(entity)
        }
        
        statusData.value = updatedData
    }

    override suspend fun delete(categoryId: Long, yearMonth: String) {
        val updatedData = statusData.value.toMutableList()
        updatedData.removeIf { it.categoryId == categoryId && it.yearMonth == yearMonth }
        statusData.value = updatedData
    }
}