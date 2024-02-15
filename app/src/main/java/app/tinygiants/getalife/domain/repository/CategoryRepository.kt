package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.HeaderEntity
import app.tinygiants.getalife.data.local.entities.HeaderWithCategoriesEntity
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getBudget(): Flow<Result<List<HeaderWithCategoriesEntity>>>

    suspend fun addHeader(headerEntity: HeaderEntity)
    suspend fun updateHeader(headerEntity: HeaderEntity)
    suspend fun deleteHeader(headerEntity: HeaderEntity)

    suspend fun addCategory(categoryEntity: CategoryEntity)
    suspend fun updateCategory(categoryEntity: CategoryEntity)
    suspend fun deleteCategory(categoryEntity: CategoryEntity)
}