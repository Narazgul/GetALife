package app.tinygiants.getalife.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import app.tinygiants.getalife.data.local.entities.GroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Query("SELECT * FROM groups")
    fun getGroupsFlow(): Flow<List<GroupEntity>>

    @Insert
    suspend fun addGroup(groupEntity: GroupEntity)

    @Update
    suspend fun updateGroup(groupEntity: GroupEntity)

    @Delete
    suspend fun deleteGroup(groupEntity: GroupEntity)

}