package app.tinygiants.getalife.domain.usecase.categories.category

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetCategoriesUseCase @Inject constructor(
    private val repository: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    operator fun invoke(): Flow<Result<List<Category>>> {
        return flow {
            repository.getCategoriesFlow()
                .catch { throwable -> emit(Result.failure(throwable)) }
                .collect { categoryEntities -> emit(Result.success(mapToCategory(categoryEntities = categoryEntities))) }
        }
    }

    private suspend fun mapToCategory(categoryEntities: List<CategoryEntity>) =
        withContext(defaultDispatcher) {
            categoryEntities.map { categoryEntity ->

                val budgetTarget = categoryEntity.budgetTarget ?: 0.00

                Category(
                    id = categoryEntity.id,
                    groupId = categoryEntity.groupId,
                    emoji = categoryEntity.emoji,
                    name = categoryEntity.name,
                    budgetTarget = Money(value = budgetTarget),
                    budgetPurpose = categoryEntity.budgetPurpose,
                    assignedMoney = Money(value = categoryEntity.assignedMoney),
                    availableMoney = Money(value = categoryEntity.availableMoney),
                    progress = EmptyProgress(),
                    listPosition = categoryEntity.listPosition,
                    isInitialCategory = categoryEntity.isInitialCategory,
                    updatedAt = categoryEntity.updatedAt,
                    createdAt = categoryEntity.createdAt
                )
            }
        }
}