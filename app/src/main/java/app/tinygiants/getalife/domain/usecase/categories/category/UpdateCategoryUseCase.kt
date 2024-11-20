package app.tinygiants.getalife.domain.usecase.categories.category

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.usecase.emoji.AddEmojiToCategoryNameUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import javax.inject.Inject

class UpdateCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
    private val addEmoji: AddEmojiToCategoryNameUseCase,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(category: Category) = withContext(defaultDispatcher) {

        val oldCategory = repository.getCategory(categoryId = category.id) ?: return@withContext

        val assignedMoneyDifference = category.assignedMoney.value - oldCategory.assignedMoney
        val newAvailableMoney = category.availableMoney.value + assignedMoneyDifference

        val categoryEntity = CategoryEntity(
            id = category.id,
            groupId = category.groupId,
            emoji = category.emoji,
            name = category.name,
            budgetTarget = category.budgetTarget.value,
            assignedMoney = category.assignedMoney.value,
            availableMoney = newAvailableMoney,
            listPosition = category.listPosition,
            isInitialCategory = false,
            updatedAt = Clock.System.now(),
            createdAt = category.createdAt
        )

        repository.updateCategory(categoryEntity)

        if (category.isInitialCategory) addEmoji(categoryEntity)
    }
}