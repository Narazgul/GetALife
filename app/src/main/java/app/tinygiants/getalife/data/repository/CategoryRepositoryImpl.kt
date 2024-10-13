package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.CategoryDao
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(private val categoryDao: CategoryDao) : CategoryRepository {

    override fun getCategories(): Flow<List<CategoryEntity>> = categoryDao.getCategories()
    override suspend fun getCategoriesInGroup(groupId: Long) = categoryDao.getCategoryInGroup(groupId = groupId)
    override suspend fun addCategory(categoryEntity: CategoryEntity) { categoryDao.addCategory(categoryEntity = categoryEntity) }
    override suspend fun updateCategory(categoryEntity: CategoryEntity) { categoryDao.updateCategory(categoryEntity) }
    override suspend fun deleteCategory(categoryEntity: CategoryEntity) { categoryDao.deleteCategory(categoryEntity) }

}