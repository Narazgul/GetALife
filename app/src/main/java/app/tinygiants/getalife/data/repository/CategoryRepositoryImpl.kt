package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.CategoryDao
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.remote.FirestoreDataSource
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.usecase.GetCurrentBudgetUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Offline-first repository for Categories.
 * Room = Single Source of Truth, Firestore = automatic sync layer.
 */
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val getCurrentBudget: GetCurrentBudgetUseCase,
    private val firestore: FirestoreDataSource,
    private val externalScope: CoroutineScope
) : CategoryRepository {

    override fun getCategoriesFlow(): Flow<List<Category>> =
        getCurrentBudget.currentBudgetIdOrDefaultFlow.flatMapLatest { budgetId ->
            categoryDao.getCategoriesFlow(budgetId).map { entities ->
                entities.map { entity -> entity.toDomain() }
            }
        }

    override suspend fun getCategoriesInGroup(groupId: Long): List<Category> {
        val budgetId = getCurrentBudget.getCurrentBudgetIdOrDefault()
        return categoryDao.getCategoriesInGroup(groupId = groupId, budgetId = budgetId).map { entity -> entity.toDomain() }
    }

    override suspend fun getCategory(categoryId: Long): Category? {
        val budgetId = getCurrentBudget.getCurrentBudgetIdOrDefault()
        return categoryDao.getCategory(categoryId = categoryId, budgetId = budgetId)?.toDomain()
    }

    override suspend fun addCategory(category: Category) {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        val entity = CategoryEntity.fromDomain(category, budgetId)
        categoryDao.addCategory(entity.copy(isSynced = false))
        syncCategoryInBackground(entity)
    }

    override suspend fun updateCategory(category: Category) {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        val entity = CategoryEntity.fromDomain(category, budgetId)
        categoryDao.updateCategory(entity.copy(isSynced = false))
        syncCategoryInBackground(entity)
    }

    override suspend fun deleteCategory(category: Category) {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        val entity = CategoryEntity.fromDomain(category, budgetId)
        categoryDao.deleteCategory(entity)
        // TODO: firestore deletion
    }

    override suspend fun getCreditCardPaymentCategory(accountId: Long): Category? {
        val categories = getCategoriesFlow().first()
        return categories.firstOrNull { it.linkedAccountId == accountId }
    }

    private fun syncCategoryInBackground(entity: CategoryEntity) {
        externalScope.async {
            try {
                firestore.saveCategory(entity)
                // Mark as synced in local database
                categoryDao.updateCategory(entity.copy(isSynced = true))
            } catch (_: Exception) {
                // Firestore handles offline persistence automatically
            }
        }
    }

    suspend fun syncWithFirestore(budgetId: String) {
        try {
            val remote = firestore.getCategories(budgetId)
            val local = categoryDao.getCategoriesFlow(budgetId).first()

            remote.forEach { remoteCategory ->
                val localCategory = local.find { it.id == remoteCategory.id }
                if (localCategory == null || remoteCategory.updatedAt > localCategory.updatedAt) {
                    categoryDao.updateCategory(remoteCategory.copy(isSynced = true))
                }
            }
        } catch (_: Exception) {
            // Handle gracefully - local remains source of truth
        }
    }
}