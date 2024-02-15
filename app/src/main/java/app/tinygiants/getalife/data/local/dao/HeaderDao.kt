package app.tinygiants.getalife.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import app.tinygiants.getalife.data.local.entities.HeaderEntity
import app.tinygiants.getalife.data.local.entities.HeaderWithCategoriesEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HeaderDao {

    @Transaction
    @Query("SELECT * FROM headers")
    fun getBudget(): Flow<List<HeaderWithCategoriesEntity>>

    @Insert
    suspend fun addHeader(headerEntity: HeaderEntity)

    @Update
    suspend fun updateHeader(headerEntity: HeaderEntity)

    @Delete
    suspend fun deleteHeader(headerEntity: HeaderEntity)

}