package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.HeaderEntity
import app.tinygiants.getalife.data.local.entities.HeaderWithCategoriesEntity
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {

    fun getBudgetFlow(): Flow<Result<List<HeaderWithCategoriesEntity>>>
    fun getCategoriesFlow(): Flow<Result<List<CategoryEntity>>>
    suspend fun getCategoriesOfHeader(headerId: Long): List<CategoryEntity>

    suspend fun addHeader(headerEntity: HeaderEntity)
    suspend fun updateHeader(headerEntity: HeaderEntity)
    suspend fun deleteHeader(headerEntity: HeaderEntity)

    suspend fun getCategory(categoryId: Long): CategoryEntity
    suspend fun addCategory(categoryEntity: CategoryEntity)
    suspend fun updateCategory(categoryEntity: CategoryEntity)
    suspend fun deleteCategory(categoryEntity: CategoryEntity)
}