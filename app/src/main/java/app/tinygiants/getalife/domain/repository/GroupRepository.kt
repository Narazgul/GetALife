package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.data.local.entities.GroupEntity
import kotlinx.coroutines.flow.Flow

interface GroupRepository {

    fun getGroupsFlow(): Flow<List<GroupEntity>>
    suspend fun getGroups(): List<GroupEntity>
    suspend fun addGroup(groupEntity: GroupEntity)
    suspend fun updateGroup(groupEntity: GroupEntity)
    suspend fun deleteGroup(groupEntity: GroupEntity)

}