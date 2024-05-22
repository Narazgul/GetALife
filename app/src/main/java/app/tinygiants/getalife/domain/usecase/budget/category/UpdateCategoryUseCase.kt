package app.tinygiants.getalife.domain.usecase.budget.category

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.repository.BudgetRepository
import app.tinygiants.getalife.domain.usecase.emoji.AddEmojiToCategoryNameUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateCategoryUseCase @Inject constructor(
    private val repository: BudgetRepository,
    private val addEmoji: AddEmojiToCategoryNameUseCase,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(category: Category) = withContext(defaultDispatcher) {

        val categoryEntity = CategoryEntity(
            id = category.id,
            headerId = category.headerId,
            emoji = category.emoji,
            name = category.name,
            budgetTarget = category.budgetTarget.value,
            budgetPurpose = category.budgetPurpose,
            assignedMoney = category.assignedMoney.value,
            availableMoney = category.availableMoney.value,
            optionalText = category.optionalText,
            listPosition = category.listPosition,
            isInitialCategory = false
        )

        repository.updateCategory(categoryEntity)

        if (category.isInitialCategory) addEmoji(categoryEntity)
    }
}