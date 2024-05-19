package app.tinygiants.getalife.domain.usecase.budget.category

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.BudgetRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: BudgetRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(): Flow<Result<List<Category>>> {
        return flow {
            repository.getCategoriesFlow()
                .catch { throwable -> emit(Result.failure(throwable)) }
                .collect { result ->
                    result.onFailure { throwable -> emit(Result.failure(throwable)) }
                    result.onSuccess { categoryEntities -> emit(Result.success(mapToCategory(categoryEntities = categoryEntities))) }
                }
        }
    }

    private suspend fun mapToCategory(categoryEntities: List<CategoryEntity>) =
        withContext(defaultDispatcher) {
            categoryEntities.map { categoryEntity ->

                val progress = (categoryEntity.availableMoney / categoryEntity.budgetTarget).toFloat()

                Category(
                    id = categoryEntity.id,
                    headerId = categoryEntity.headerId,
                    emoji = categoryEntity.emoji,
                    name = categoryEntity.name,
                    budgetTarget = Money(value = categoryEntity.budgetTarget),
                    budgetPurpose = categoryEntity.budgetPurpose,
                    assignedMoney = Money(value = categoryEntity.assignedMoney),
                    availableMoney = Money(value = categoryEntity.availableMoney),
                    progress = progress,
                    optionalText = categoryEntity.optionalText,
                    listPosition = categoryEntity.listPosition,
                    isInitialCategory = categoryEntity.isInitialCategory
                )
            }
        }
}