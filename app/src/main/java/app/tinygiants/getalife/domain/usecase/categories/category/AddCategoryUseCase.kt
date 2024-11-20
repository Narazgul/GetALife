package app.tinygiants.getalife.domain.usecase.categories.category

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.usecase.emoji.AddEmojiToCategoryNameUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import javax.inject.Inject
import kotlin.random.Random

class AddCategoryUseCase @Inject constructor(
    private val repository: CategoryRepository,
    private val addEmoji: AddEmojiToCategoryNameUseCase,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(groupId: Long, categoryName: String, isInitialCategory: Boolean = false) {

        val categories = repository.getCategoriesInGroup(groupId = groupId)

        val categoryEntity = withContext(defaultDispatcher) {

            val highestListPosition = categories.maxOfOrNull { it.listPosition }
            val endOfListPosition = if (highestListPosition == null) 0 else highestListPosition + 1
            val trimmedCategoryName = categoryName.trim()

            CategoryEntity(
                id = Random.nextLong(),
                groupId = groupId,
                emoji = "",
                name = trimmedCategoryName,
                budgetTarget = 0.0,
                assignedMoney = 0.0,
                availableMoney = 0.0,
                listPosition = endOfListPosition,
                isInitialCategory = isInitialCategory,
                updatedAt = Clock.System.now(),
                createdAt = Clock.System.now()
            )
        }

        repository.addCategory(categoryEntity = categoryEntity)

        if (!isInitialCategory) addEmoji(categoryEntity = categoryEntity)
    }
}