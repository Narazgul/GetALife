package app.tinygiants.getalife.domain.usecase.category

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.usecase.emoji.AddEmojiToCategoryNameUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
    private val addEmoji: AddEmojiToCategoryNameUseCase,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(category: Category) {

        val categoryEntity = withContext(defaultDispatcher) {
            CategoryEntity(
                id = category.id,
                headerId = category.headerId,
                name = category.name,
                budgetTarget = category.budgetTarget.value,
                availableMoney = category.availableMoney.value,
                optionalText = category.optionalText,
                listPosition = category.listPosition,
                isEmptyCategory = false
            )
        }

        repository.updateCategory(categoryEntity = categoryEntity)

        addEmoji(categoryEntity = categoryEntity)
    }
}