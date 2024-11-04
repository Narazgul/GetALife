package app.tinygiants.getalife.data.local.dao

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class CategoryDaoFake: CategoryDao {

    val categories = MutableStateFlow<List<CategoryEntity>>(emptyList())

    override fun getCategoriesFlow(): Flow<List<CategoryEntity>> = categories

    override suspend fun getCategoriesInGroup(groupId: Long): List<CategoryEntity> {
        return categories.value.filter { it.groupId == groupId }
    }

    override suspend fun getCategory(categoryId: Long): CategoryEntity? =
        categories.value.find { it.id == categoryId }

    override suspend fun addCategory(categoryEntity: CategoryEntity) {
        val updatedCategories = categories.value.toMutableList().apply {
            add(categoryEntity)
        }
        categories.value = updatedCategories
    }

    override suspend fun updateCategory(categoryEntity: CategoryEntity) {
        val updatedCategories = categories.value.toMutableList()
        val index = updatedCategories.indexOfFirst { it.id == categoryEntity.id }
        if (index != -1) {
            updatedCategories[index] = categoryEntity
            categories.value = updatedCategories
        } else {
            throw IllegalArgumentException("Account with id ${categoryEntity.id} not found")
        }
    }

    override suspend fun deleteCategory(categoryEntity: CategoryEntity) {
        val updatedCategories = categories.value.toMutableList().apply {
            removeIf { it.id == categoryEntity.id }
        }
        categories.value = updatedCategories
    }
}