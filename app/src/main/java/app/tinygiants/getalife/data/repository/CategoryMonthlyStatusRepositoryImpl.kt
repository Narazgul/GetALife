package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.CategoryMonthlyStatusDao
import app.tinygiants.getalife.data.local.entities.CategoryMonthlyStatusEntity
import app.tinygiants.getalife.domain.model.CategoryMonthlyStatus
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.CategoryMonthlyStatusRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.usecase.GetCurrentBudgetUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.YearMonth
import javax.inject.Inject

/**
 * Repository implementation for category monthly status.
 * Only handles raw data operations - no business logic or calculations.
 */
class CategoryMonthlyStatusRepositoryImpl @Inject constructor(
    private val statusDao: CategoryMonthlyStatusDao,
    private val categoryRepository: CategoryRepository,
    private val getCurrentBudget: GetCurrentBudgetUseCase
) : CategoryMonthlyStatusRepository {

    override suspend fun getStatus(categoryId: Long, yearMonth: YearMonth): CategoryMonthlyStatus? {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        val entity = statusDao.getStatusData(categoryId, yearMonth.toString(), budgetId)
        return if (entity != null) {
            val category = categoryRepository.getCategory(categoryId)
                ?: return null // Category doesn't exist

            entity.toDomain(
                category = category,
                progress = EmptyProgress(), // Will be calculated in use case
                suggestedAmount = null // Will be calculated in use case
            )
        } else null
    }

    override suspend fun getStatusForMonth(yearMonth: YearMonth): List<CategoryMonthlyStatus> {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        val entities = statusDao.getStatusDataForMonth(yearMonth.toString(), budgetId)
        val categories = categoryRepository.getCategoriesFlow().first().associateBy { it.id }

        return entities.mapNotNull { entity ->
            val category = categories[entity.categoryId] ?: return@mapNotNull null

            entity.toDomain(
                category = category,
                progress = EmptyProgress(), // Will be calculated in use case
                suggestedAmount = null // Will be calculated in use case
            )
        }
    }

    override suspend fun saveStatus(status: CategoryMonthlyStatus, yearMonth: YearMonth) {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        statusDao.insertOrUpdate(CategoryMonthlyStatusEntity.fromDomain(status, yearMonth, budgetId = budgetId))
    }

    override suspend fun saveStatus(status: CategoryMonthlyStatus, yearMonth: YearMonth, carryOverFromPrevious: Money) {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        statusDao.insertOrUpdate(
            CategoryMonthlyStatusEntity.fromDomain(
                status,
                yearMonth,
                budgetId,
                carryOverFromPrevious
            )
        )
    }

    override suspend fun deleteStatus(categoryId: Long, yearMonth: YearMonth) {
        statusDao.delete(categoryId, yearMonth.toString())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getStatusForMonthFlow(yearMonth: YearMonth): Flow<List<CategoryMonthlyStatus>> =
        getCurrentBudget.currentBudgetIdOrDefaultFlow.flatMapLatest { budgetId ->
            statusDao.getStatusDataForMonthFlow(yearMonth.toString(), budgetId).map { entities ->
                val categories = categoryRepository.getCategoriesFlow().first().associateBy { it.id }

                entities.mapNotNull { entity ->
                    val category = categories[entity.categoryId] ?: return@mapNotNull null

                    entity.toDomain(
                        category = category,
                        progress = EmptyProgress(), // Will be calculated in use case
                        suggestedAmount = null // Will be calculated in use case
                    )
                }
            }
        }

    override suspend fun getAllStatuses(): List<CategoryMonthlyStatus> {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        val entities = statusDao.getAllStatusData(budgetId)
        val categories = categoryRepository.getCategoriesFlow().first().associateBy { it.id }

        return entities.mapNotNull { entity ->
            val category = categories[entity.categoryId] ?: return@mapNotNull null

            entity.toDomain(
                category = category,
                progress = EmptyProgress(), // Will be calculated in use case
                suggestedAmount = null // Will be calculated in use case
            )
        }
    }
}