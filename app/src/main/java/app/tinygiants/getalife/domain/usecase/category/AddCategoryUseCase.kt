package app.tinygiants.getalife.domain.usecase.category

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.usecase.emoji.AddEmojiToCategoryNameUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

class AddCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
    private val addEmoji: AddEmojiToCategoryNameUseCase,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(headerId: Long, categoryName: String, isEmptyCategory: Boolean = false) {

        val categories = repository.getCategoriesBy(headerId = headerId)

        val categoryEntity = withContext(defaultDispatcher) {

            val highestListPosition = categories.maxOfOrNull { it.listPosition }
            val endOfListPosition = if (highestListPosition == null) 0 else highestListPosition + 1

            CategoryEntity(
                id = Random.nextLong(),
                headerId = headerId,
                name = categoryName,
                budgetTarget = 0.00,
                availableMoney = 0.00,
                optionalText = "",
                listPosition = endOfListPosition,
                isEmptyCategory = isEmptyCategory
            )
        }

        repository.addCategory(categoryEntity = categoryEntity)

        addEmoji(categoryEntity = categoryEntity)
    }
}