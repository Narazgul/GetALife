package app.tinygiants.getalife.domain.usecase.budget.category

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateAssignedMoneyUseCase @Inject constructor(
    private val updateCategory: UpdateCategoryUseCase,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(category: Category, newAssignedMoney: Money) =
        withContext(defaultDispatcher) {

            val previousAssignedMoney = category.assignedMoney
            val assignedMoneyDifference = when {
                previousAssignedMoney == newAssignedMoney -> Money(value = 0.00)
                else -> newAssignedMoney - previousAssignedMoney
            }

            val updatedAvailableMoney = category.availableMoney + assignedMoneyDifference
            val updatedCategory = category.copy(assignedMoney = newAssignedMoney, availableMoney = updatedAvailableMoney)

            updateCategory(category = updatedCategory)
        }
}