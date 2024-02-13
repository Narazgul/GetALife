package app.tinygiants.getalife.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Update
import app.tinygiants.getalife.data.local.entities.CategoryEntity

@Dao
interface CategoryDao {

    @Insert
    suspend fun addCategory(categoryEntity: CategoryEntity)

    @Update
    suspend fun updateCategory(categoryEntity: CategoryEntity)

    @Delete
    suspend fun deleteCategory(categoryEntity: CategoryEntity)

}