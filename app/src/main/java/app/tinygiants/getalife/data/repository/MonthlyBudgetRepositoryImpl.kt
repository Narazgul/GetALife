package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.MonthlyBudgetDao
import app.tinygiants.getalife.data.local.entities.MonthlyBudgetEntity
import app.tinygiants.getalife.domain.model.MonthlyBudget
import app.tinygiants.getalife.domain.repository.MonthlyBudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.YearMonth
import javax.inject.Inject

class MonthlyBudgetRepositoryImpl @Inject constructor(
    private val monthlyBudgetDao: MonthlyBudgetDao
) : MonthlyBudgetRepository {

    override suspend fun getMonthlyBudget(categoryId: Long, yearMonth: YearMonth): MonthlyBudget? =
        monthlyBudgetDao.getMonthlyBudget(categoryId, yearMonth.toString())?.toDomain()

    override suspend fun getMonthlyBudgetsForMonth(yearMonth: YearMonth): List<MonthlyBudget> =
        monthlyBudgetDao.getMonthlyBudgetsForMonth(yearMonth.toString()).map { it.toDomain() }

    override suspend fun createOrUpdateMonthlyBudget(monthlyBudget: MonthlyBudget) {
        monthlyBudgetDao.insertOrUpdate(MonthlyBudgetEntity.fromDomain(monthlyBudget))
    }

    override suspend fun deleteMonthlyBudget(categoryId: Long, yearMonth: YearMonth) {
        monthlyBudgetDao.delete(categoryId, yearMonth.toString())
    }

    override fun getMonthlyBudgetsFlow(yearMonth: YearMonth): Flow<List<MonthlyBudget>> =
        monthlyBudgetDao.getMonthlyBudgetsFlow(yearMonth.toString()).map { entities ->
            entities.map { it.toDomain() }
        }
}