package app.tinygiants.getalife.data

import app.tinygiants.getalife.data.local.dao.CategoryDao
import app.tinygiants.getalife.data.local.dao.HeaderDao
import app.tinygiants.getalife.data.local.dao.mapToGroups
import app.tinygiants.getalife.data.local.entities.mapToCategoryEntity
import app.tinygiants.getalife.data.local.entities.mapToHeaderEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Header
import app.tinygiants.getalife.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val headerDao: HeaderDao,
    private val categoryDao: CategoryDao,
    @Default private val defaultDispatcher: CoroutineDispatcher
) : CategoryRepository {

    override fun getBudget(): Flow<Result<List<Group>>> {
        return flow {
            headerDao.getBudget()
                .catch { exception -> emit(Result.failure(exception)) }
                .collect { headersWithCategories ->
                    val groups = mapToGroups(headersWithCategories)
                    emit(Result.success(groups))
                }
        }
    }

    override suspend fun toggleIsExpanded(header: Header) {
        val headerEntity = withContext(defaultDispatcher) { mapToHeaderEntity(header = header) }
        headerDao.toggleIsExpanded(headerEntity = headerEntity)
    }

    override suspend fun addHeader(name: String) {
        val headerEntity = withContext(defaultDispatcher) {
            mapToHeaderEntity(
                name = name,
                isExpanded = true
            )
        }
        headerDao.addHeader(headerEntity = headerEntity)
    }

    override suspend fun updateHeader(header: Header) {
        val headerEntity = withContext(defaultDispatcher) { mapToHeaderEntity(header = header) }
        headerDao.updateHeader(headerEntity = headerEntity)
    }

    override suspend fun deleteHeader(header: Header) {
        val headerEntity = withContext(defaultDispatcher) { mapToHeaderEntity(header = header) }
        headerDao.deleteHeader(headerEntity = headerEntity)
    }

    override suspend fun addCategory(headerId: Long, categoryName: String) {
        val categoryEntity = withContext(defaultDispatcher) {
            mapToCategoryEntity(
                headerId = headerId,
                categoryName = categoryName
            )
        }
        categoryDao.addCategory(categoryEntity = categoryEntity)
    }

    override suspend fun updateCategory(category: Category) {
        val categoryEntity = withContext(defaultDispatcher) { mapToCategoryEntity(category = category) }
        categoryDao.updateCategory(categoryEntity)
    }

    override suspend fun deleteCategory(category: Category) {
        val categoryEntity = withContext(defaultDispatcher) { mapToCategoryEntity(category = category) }
        categoryDao.deleteCategory(categoryEntity)
    }
}