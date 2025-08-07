package app.tinygiants.getalife.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Transaction
    @Query("SELECT * FROM categories WHERE budgetId = :budgetId")
    fun getCategoriesFlow(budgetId: String): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE groupId == :groupId AND budgetId = :budgetId")
    suspend fun getCategoriesInGroup(groupId: Long, budgetId: String): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id == :categoryId AND budgetId = :budgetId")
    suspend fun getCategory(categoryId: Long, budgetId: String): CategoryEntity?

    @Insert
    suspend fun addCategory(categoryEntity: CategoryEntity)

    @Update
    suspend fun updateCategory(categoryEntity: CategoryEntity)

    @Delete
    suspend fun deleteCategory(categoryEntity: CategoryEntity)

}