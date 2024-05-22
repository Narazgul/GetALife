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

            val oldAssignedMoney = category.assignedMoney
            val deltaAssignedMoney = when {
                oldAssignedMoney == newAssignedMoney -> newAssignedMoney
                else -> newAssignedMoney - oldAssignedMoney
            }
            val newAvailableMoney = category.availableMoney + deltaAssignedMoney
            val updatedCategory = category.copy(assignedMoney = newAssignedMoney, availableMoney = newAvailableMoney)

            updateCategory(category = updatedCategory)
        }
}