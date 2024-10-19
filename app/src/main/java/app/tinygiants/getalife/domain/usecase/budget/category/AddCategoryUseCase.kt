package app.tinygiants.getalife.domain.usecase.budget.category

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.BudgetPurpose
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

            CategoryEntity(
                id = Random.nextLong(),
                groupId = groupId,
                emoji = "",
                name = categoryName,
                budgetTarget = null,
                budgetPurpose = BudgetPurpose.Unknown,
                assignedMoney = 0.00,
                availableMoney = 0.00,
                optionalText = "",
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