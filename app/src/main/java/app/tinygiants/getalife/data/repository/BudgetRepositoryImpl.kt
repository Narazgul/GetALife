package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.BudgetDao
import app.tinygiants.getalife.data.local.entities.BudgetEntity
import app.tinygiants.getalife.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(private val budgetDao: BudgetDao) : BudgetRepository {

    override fun getBudgets(): Flow<List<BudgetEntity>> = budgetDao.getBudgets()
    override suspend fun getBudget(): BudgetEntity = budgetDao.getBudget()
    override suspend fun addBudget(budgetEntity: BudgetEntity) = budgetDao.addBudget(budgetEntity = budgetEntity)
    override suspend fun updateBudget(budgetEntity: BudgetEntity) = budgetDao.updateBudget(budgetEntity = budgetEntity)

}