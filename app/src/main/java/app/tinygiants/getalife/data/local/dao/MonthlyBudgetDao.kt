package app.tinygiants.getalife.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.tinygiants.getalife.data.local.entities.MonthlyBudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyBudgetDao {

    @Query("SELECT * FROM monthly_budgets WHERE categoryId = :categoryId AND yearMonth = :yearMonth")
    suspend fun getMonthlyBudget(categoryId: Long, yearMonth: String): MonthlyBudgetEntity?

    @Query("SELECT * FROM monthly_budgets WHERE yearMonth = :yearMonth")
    suspend fun getMonthlyBudgetsForMonth(yearMonth: String): List<MonthlyBudgetEntity>

    @Query("SELECT * FROM monthly_budgets WHERE yearMonth = :yearMonth")
    fun getMonthlyBudgetsFlow(yearMonth: String): Flow<List<MonthlyBudgetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(monthlyBudget: MonthlyBudgetEntity)

    @Query("DELETE FROM monthly_budgets WHERE categoryId = :categoryId AND yearMonth = :yearMonth")
    suspend fun delete(categoryId: Long, yearMonth: String)
}