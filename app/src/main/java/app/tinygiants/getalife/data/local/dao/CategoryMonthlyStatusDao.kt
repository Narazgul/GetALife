package app.tinygiants.getalife.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import app.tinygiants.getalife.data.local.entities.CategoryMonthlyStatusEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryMonthlyStatusDao {

    @Query("SELECT * FROM category_monthly_status WHERE categoryId = :categoryId AND yearMonth = :yearMonth AND budgetId = :budgetId")
    suspend fun getStatusData(categoryId: Long, yearMonth: String, budgetId: String): CategoryMonthlyStatusEntity?

    @Query("SELECT * FROM category_monthly_status WHERE yearMonth = :yearMonth AND budgetId = :budgetId")
    suspend fun getStatusDataForMonth(yearMonth: String, budgetId: String): List<CategoryMonthlyStatusEntity>

    @Query("SELECT * FROM category_monthly_status WHERE yearMonth = :yearMonth AND budgetId = :budgetId")
    fun getStatusDataForMonthFlow(yearMonth: String, budgetId: String): Flow<List<CategoryMonthlyStatusEntity>>

    @Query("SELECT * FROM category_monthly_status WHERE budgetId = :budgetId")
    suspend fun getAllStatusData(budgetId: String): List<CategoryMonthlyStatusEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entity: CategoryMonthlyStatusEntity)

    @Query("DELETE FROM category_monthly_status WHERE categoryId = :categoryId AND yearMonth = :yearMonth")
    suspend fun delete(categoryId: Long, yearMonth: String)
}