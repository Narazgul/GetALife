package app.tinygiants.getalife.domain.usecase.categories.category

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.repository.CategoryRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeleteCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(category: Category) {

        val categoryEntity = withContext(defaultDispatcher) {
            CategoryEntity(
                id = category.id,
                groupId = category.groupId,
                emoji = category.emoji,
                name = category.name,
                budgetTarget = category.budgetTarget?.value,
                budgetPurpose = category.budgetPurpose,
                assignedMoney = category.assignedMoney.value,
                availableMoney = category.availableMoney.value,
                optionalText = category.optionalText,
                listPosition = category.listPosition,
                isInitialCategory = category.isInitialCategory,
                updatedAt = category.updatedAt,
                createdAt = category.createdAt
            )
        }

        repository.deleteCategory(categoryEntity = categoryEntity)
    }
}