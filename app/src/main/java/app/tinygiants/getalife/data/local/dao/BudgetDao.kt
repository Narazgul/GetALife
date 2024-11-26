package app.tinygiants.getalife.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.tinygiants.getalife.data.local.entities.BudgetEntity
import app.tinygiants.getalife.data.local.entities.DEFAULT_BUDGET_ID
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Transaction
    @Query("SELECT * FROM budget")
    fun getBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budget WHERE id == :budgetId")
    suspend fun getBudget(budgetId: Long = DEFAULT_BUDGET_ID): BudgetEntity

    @Insert
    suspend fun addBudget(budgetEntity: BudgetEntity)

    @Update
    suspend fun updateBudget(budgetEntity: BudgetEntity)

}