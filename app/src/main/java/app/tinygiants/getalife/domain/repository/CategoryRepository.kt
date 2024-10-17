package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    fun getCategoriesFlow(): Flow<List<CategoryEntity>>
    suspend fun getCategoriesInGroup(groupId: Long): List<CategoryEntity>
    suspend fun addCategory(categoryEntity: CategoryEntity)
    suspend fun updateCategory(categoryEntity: CategoryEntity)
    suspend fun deleteCategory(categoryEntity: CategoryEntity)

}