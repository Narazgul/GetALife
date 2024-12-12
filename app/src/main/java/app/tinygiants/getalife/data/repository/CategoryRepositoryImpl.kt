package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.CategoryDao
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    override fun getCategoriesFlow(): Flow<List<Category>> =
        categoryDao.getCategoriesFlow().map { entities -> entities.map { entity -> entity.toDomain() } }

    override suspend fun getCategoriesInGroup(groupId: Long): List<Category> =
        categoryDao.getCategoriesInGroup(groupId = groupId).map {  entity -> entity.toDomain() }

    override suspend fun getCategory(categoryId: Long): Category? =
        categoryDao.getCategory(categoryId = categoryId)?.toDomain()

    override suspend fun addCategory(category: Category) =
        categoryDao.addCategory(CategoryEntity.fromDomain(category))

    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(CategoryEntity.fromDomain(category))
    }

    override suspend fun deleteCategory(category: Category) =
        categoryDao.deleteCategory(CategoryEntity.fromDomain(category))
}