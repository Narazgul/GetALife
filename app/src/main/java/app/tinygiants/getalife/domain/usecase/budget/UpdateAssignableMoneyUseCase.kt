package app.tinygiants.getalife.domain.usecase.budget

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.repository.BudgetRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UpdateAssignableMoneyUseCase @Inject constructor(
    private val repository: BudgetRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(newAmount: Money) {
        withContext(defaultDispatcher) {
            val currentBudget = repository.getBudget()

            val updatedAssignableMoney = currentBudget.readyToAssign + newAmount.value
            val updatedBudget = currentBudget.copy(readyToAssign = updatedAssignableMoney)

            repository.updateBudget(updatedBudget)
        }
    }
}