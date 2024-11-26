package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.data.local.entities.BudgetEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.Clock

class BudgetRepositoryFake: BudgetRepository {

    var initialBudget = BudgetEntity(
            readyToAssign = 0.0,
            updatedAt = Clock.System.now(),
            createdAt = Clock.System.now()
        )

    val budgetsFlow = MutableStateFlow(listOf(initialBudget))

    override fun getBudgets(): Flow<List<BudgetEntity>> = budgetsFlow

    override suspend fun getBudget(): BudgetEntity = budgetsFlow.value.first()

    override suspend fun addBudget(budgetEntity: BudgetEntity) {
        budgetsFlow.value = budgetsFlow.value.toMutableList().apply { add(budgetEntity) }
    }

    override suspend fun updateBudget(budgetEntity: BudgetEntity) {
        budgetsFlow.value = budgetsFlow.value.toMutableList().apply {
            val index = indexOfFirst { it.id == budgetEntity.id }
            if (index != -1) {
                set(index, budgetEntity)
            }
        }
    }
}