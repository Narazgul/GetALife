package app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.usecase.emoji.AddEmojiToCategoryNameUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Clock

class AddCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
    private val addEmoji: AddEmojiToCategoryNameUseCase,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(groupId: Long, categoryName: String, isInitialCategory: Boolean = false) {

        val categories = repository.getCategoriesInGroup(groupId = groupId)

        val category = withContext(defaultDispatcher) {

            val highestListPosition = categories.maxOfOrNull { it.listPosition }
            val endOfListPosition = if (highestListPosition == null) 0 else highestListPosition + 1
            val trimmedCategoryName = categoryName.trim()

            val timeOfCreation = Clock.System.now()

            Category(
                id = Random.nextLong(),
                groupId = groupId,
                emoji = "",
                name = trimmedCategoryName,
                budgetTarget = EmptyMoney(),
                monthlyTargetAmount = null,
                targetMonthsRemaining = null,
                listPosition = endOfListPosition,
                isInitialCategory = isInitialCategory,
                updatedAt = timeOfCreation,
                createdAt = timeOfCreation
            )
        }
        repository.addCategory(category = category)

        if (!isInitialCategory) addEmoji(category = category)
    }
}