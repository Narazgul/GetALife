package app.tinygiants.getalife.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import app.tinygiants.getalife.data.local.entities.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE firebaseUserId = :firebaseUserId")
    fun getBudgetsFlow(firebaseUserId: String): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE id = :budgetId")
    suspend fun getBudget(budgetId: String): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE firebaseUserId = :firebaseUserId")
    suspend fun getBudgets(firebaseUserId: String): List<BudgetEntity>

    @Insert
    suspend fun addBudget(budgetEntity: BudgetEntity)

    @Update
    suspend fun updateBudget(budgetEntity: BudgetEntity)

    @Delete
    suspend fun deleteBudget(budgetEntity: BudgetEntity)

    // Offline-first sync queries
    @Query("SELECT * FROM budgets WHERE isSynced = 0")
    suspend fun getUnsyncedBudgets(): List<BudgetEntity>

    @Query("UPDATE budgets SET isSynced = 1, lastSyncAt = :syncTime WHERE id = :budgetId")
    suspend fun markBudgetAsSynced(budgetId: String, syncTime: Long)
}