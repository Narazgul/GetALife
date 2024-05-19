package app.tinygiants.getalife.domain.usecase.budget.category

import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.repository.BudgetRepository
import app.tinygiants.getalife.domain.usecase.emoji.AddEmojiToCategoryNameUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.abs

class UpdateCategoryUseCase @Inject constructor(
    private val repository: BudgetRepository,
    private val addEmoji: AddEmojiToCategoryNameUseCase,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(category: Category) = withContext(defaultDispatcher) {

        val categoryBeforeUpdate = repository
            .getCategoriesOfHeader(category.headerId)
            .find { categoryEntity -> categoryEntity.id == category.id }

        val updatedAvailableMoney = calculateAvailableMoney(
            assignedMoneyBeforeUpdate = categoryBeforeUpdate?.assignedMoney,
            assignedMoneyValue = category.assignedMoney.value,
            availableMoney = category.availableMoney.value
        )

        val categoryEntity = CategoryEntity(
            id = category.id,
            headerId = category.headerId,
            emoji = category.emoji,
            name = category.name,
            budgetTarget = category.budgetTarget.value,
            budgetPurpose = category.budgetPurpose,
            assignedMoney = category.assignedMoney.value,
            availableMoney = updatedAvailableMoney,
            optionalText = category.optionalText,
            listPosition = category.listPosition,
            isInitialCategory = false
        )

        repository.updateCategory(categoryEntity)

        if (category.isInitialCategory) { addEmoji(categoryEntity) }
    }

    private fun calculateAvailableMoney(
        assignedMoneyBeforeUpdate: Double?,
        assignedMoneyValue: Double,
        availableMoney: Double
    ): Double {
        if (assignedMoneyBeforeUpdate == null) return assignedMoneyValue

        val assignedMoneyDifference = abs(assignedMoneyValue - assignedMoneyBeforeUpdate)

        return when {
            assignedMoneyBeforeUpdate < assignedMoneyValue -> availableMoney + assignedMoneyDifference
            assignedMoneyBeforeUpdate > assignedMoneyValue -> availableMoney - assignedMoneyDifference
            else -> availableMoney
        }
    }
}