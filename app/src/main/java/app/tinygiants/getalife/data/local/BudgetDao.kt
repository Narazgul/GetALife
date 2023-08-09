package app.tinygiants.getalife.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import app.tinygiants.getalife.data.local.entities.BudgetEntity
import app.tinygiants.getalife.data.local.entities.CategoryGroupEntity
import app.tinygiants.getalife.data.local.entities.HeaderEntity
import app.tinygiants.getalife.data.local.entities.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Insert
    suspend fun insertHeader(header: HeaderEntity): Long

    @Insert
    suspend fun insertItem(item: ItemEntity): Long

    @Update
    suspend fun updateHeader(header: HeaderEntity)

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Delete
    suspend fun deleteCategoryGroup(categoryGroup: CategoryGroupEntity)

    @Delete
    suspend fun deleteHeader(header: HeaderEntity)

    @Delete
    suspend fun deleteItem(item: ItemEntity)
}