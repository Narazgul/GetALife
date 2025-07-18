package app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.usecase.emoji.AddEmojiToCategoryNameUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Clock

class UpdateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val addEmoji: AddEmojiToCategoryNameUseCase,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(updatedCategory: Category) {

        val updatedAvailableMoney = calculateUpdatedAvailableMoney(updatedCategory = updatedCategory)

        val category = updatedCategory.copy(
            availableMoney = updatedAvailableMoney,
            isInitialCategory = false,
            updatedAt = Clock.System.now()
        )

        categoryRepository.updateCategory(category = category)

        if (updatedCategory.isInitialCategory) addEmoji(category)
    }

    private suspend fun calculateUpdatedAvailableMoney(updatedCategory: Category): Money {

        val categoryBeforeUpdate =
            categoryRepository.getCategory(categoryId = updatedCategory.id) ?: return updatedCategory.availableMoney

        return withContext(defaultDispatcher) {
            val assignedMoneyBeforeUpdate = categoryBeforeUpdate.assignedMoney
            val availableMoneyBeforeUpdate = categoryBeforeUpdate.availableMoney
            val newAssignedMoney = updatedCategory.assignedMoney

            val differencePreviousAndNewAssignedMoney = newAssignedMoney - assignedMoneyBeforeUpdate
            availableMoneyBeforeUpdate + differencePreviousAndNewAssignedMoney
        }
    }
}