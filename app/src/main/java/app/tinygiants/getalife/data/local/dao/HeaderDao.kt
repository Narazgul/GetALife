package app.tinygiants.getalife.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.HeaderEntity
import app.tinygiants.getalife.data.local.entities.HeaderWithCategoriesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeaderDao {

    @Transaction
    @Query("SELECT * FROM headers")
    fun getBudgetFlow(): Flow<List<HeaderWithCategoriesEntity>>

    @Transaction
    @Query("SELECT * FROM categories WHERE headerId == :headerId")
    suspend fun getCategoriesByHeader(headerId: Long): List<CategoryEntity>

    @Insert
    suspend fun addHeader(headerEntity: HeaderEntity)

    @Update
    suspend fun updateHeader(headerEntity: HeaderEntity)

    @Delete
    suspend fun deleteHeader(headerEntity: HeaderEntity)

}