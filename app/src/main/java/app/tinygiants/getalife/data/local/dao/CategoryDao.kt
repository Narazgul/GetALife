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
    @Query("SELECT * FROM categories")
    fun getCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE groupId == :groupId")
    suspend fun getCategoriesInGroup(groupId: Long): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id == :categoryId")
    suspend fun getCategory(categoryId: Long): CategoryEntity

    @Insert
    suspend fun addCategory(categoryEntity: CategoryEntity)

    @Update
    suspend fun updateCategory(categoryEntity: CategoryEntity)

    @Delete
    suspend fun deleteCategory(categoryEntity: CategoryEntity)

}