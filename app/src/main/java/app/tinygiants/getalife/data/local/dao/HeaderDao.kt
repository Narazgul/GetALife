package app.tinygiants.getalife.data.local.dao

import androidx.room.*
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.HeaderEntity
import app.tinygiants.getalife.data.local.entities.HeaderWithCategoriesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeaderDao {

    @Transaction
    @Query("SELECT * FROM headers")
    fun getBudget(): Flow<List<HeaderWithCategoriesEntity>>

    @Transaction
    @Query("SELECT * FROM categories WHERE headerId == :headerId")
    suspend fun getCategoriesBy(headerId: Long): List<CategoryEntity>

    @Insert
    suspend fun addHeader(headerEntity: HeaderEntity)

    @Update
    suspend fun updateHeader(headerEntity: HeaderEntity)

    @Delete
    suspend fun deleteHeader(headerEntity: HeaderEntity)

}