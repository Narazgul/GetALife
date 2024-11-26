package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.data.local.entities.BudgetEntity
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {

    fun getBudgets(): Flow<List<BudgetEntity>>
    suspend fun getBudget(): BudgetEntity
    suspend fun addBudget(budgetEntity: BudgetEntity)
    suspend fun updateBudget(budgetEntity: BudgetEntity)

}