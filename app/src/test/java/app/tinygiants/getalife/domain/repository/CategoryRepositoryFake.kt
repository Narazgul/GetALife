package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class CategoryRepositoryFake : CategoryRepository {

    val categories = MutableStateFlow<List<CategoryEntity>>(emptyList())

    override fun getCategoriesFlow(): Flow<List<CategoryEntity>> = categories

    override suspend fun getCategoriesInGroup(groupId: Long) =
        categories.value.filter { it.groupId == groupId }

    override suspend fun getCategory(categoryId: Long): CategoryEntity =
        categories.value.find { it.id == categoryId } ?:
        throw NoSuchElementException("CategoryEntity with categoryId: $categoryId not found")

    override suspend fun addCategory(categoryEntity: CategoryEntity) =
        categories.update { it + categoryEntity }

    override suspend fun updateCategory(categoryEntity: CategoryEntity) = categories.update { current ->
        current.map { if (it.id == categoryEntity.id) categoryEntity else it }
    }

    override suspend fun deleteCategory(categoryEntity: CategoryEntity) =
        categories.update { it.filterNot { entity -> entity.id == categoryEntity.id } }
}