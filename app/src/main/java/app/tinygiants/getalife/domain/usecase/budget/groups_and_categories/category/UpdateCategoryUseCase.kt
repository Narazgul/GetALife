package app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.usecase.emoji.AddEmojiToCategoryNameUseCase
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject
import kotlin.time.Clock

class UpdateCategoryUseCase @Inject constructor(
    private val categoryRepository: CategoryRepository,
    private val addEmoji: AddEmojiToCategoryNameUseCase,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(updatedCategory: Category) {
        // TODO: Update-Logik muss auf MonthlyBudget umgestellt werden
        val category = updatedCategory.copy(
            isInitialCategory = false,
            updatedAt = Clock.System.now()
        )

        categoryRepository.updateCategory(category = category)

        if (updatedCategory.isInitialCategory) addEmoji(category)
    }

}