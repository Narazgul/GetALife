package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.CategoryDao
import app.tinygiants.getalife.data.local.dao.HeaderDao
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.HeaderEntity
import app.tinygiants.getalife.data.local.entities.HeaderWithCategoriesEntity
import app.tinygiants.getalife.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val headerDao: HeaderDao,
    private val categoryDao: CategoryDao
) : BudgetRepository {

    override fun getBudgetFlow(): Flow<Result<List<HeaderWithCategoriesEntity>>> =
        flow {
            headerDao.getBudgetFlow()
                .catch { exception -> emit(Result.failure(exception)) }
                .collect { headersWithCategories -> emit(Result.success(headersWithCategories)) }
        }
    override fun getCategoriesFlow(): Flow<Result<List<CategoryEntity>>> = flow {
        categoryDao.getCategories()
            .catch { exception -> emit(Result.failure(exception)) }
            .collect { categoryEntities -> emit(Result.success(categoryEntities)) }
    }
    override suspend fun getCategoriesByHeader(headerId: Long) = headerDao.getCategoriesByHeader(headerId = headerId)

    override suspend fun addHeader(headerEntity: HeaderEntity) { headerDao.addHeader(headerEntity = headerEntity) }
    override suspend fun updateHeader(headerEntity: HeaderEntity) { headerDao.updateHeader(headerEntity = headerEntity) }
    override suspend fun deleteHeader(headerEntity: HeaderEntity) { headerDao.deleteHeader(headerEntity = headerEntity) }

    override suspend fun getCategory(categoryId: Long) = categoryDao.getCategory(categoryId = categoryId)
    override suspend fun addCategory(categoryEntity: CategoryEntity) { categoryDao.addCategory(categoryEntity = categoryEntity) }
    override suspend fun updateCategory(categoryEntity: CategoryEntity) { categoryDao.updateCategory(categoryEntity) }
    override suspend fun deleteCategory(categoryEntity: CategoryEntity) { categoryDao.deleteCategory(categoryEntity) }
}