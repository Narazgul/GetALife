package app.tinygiants.getalife.data.local.dao

import app.tinygiants.getalife.data.local.entities.GroupEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class GroupDaoFake : GroupDao {

    val groups = MutableStateFlow<List<GroupEntity>>(emptyList())

    override fun getGroupsFlow(): Flow<List<GroupEntity>> = groups

    override suspend fun addGroup(groupEntity: GroupEntity) {
        val updatedGroups = groups.value.toMutableList()
        updatedGroups.add(groupEntity)
        groups.value = updatedGroups
    }

    override suspend fun updateGroup(groupEntity: GroupEntity) {
        val updatedGroups = groups.value.toMutableList()
        val index = updatedGroups.indexOfFirst { it.id == groupEntity.id }
        if (index != -1) {
            updatedGroups[index] = groupEntity
            groups.value = updatedGroups
        }
    }

    override suspend fun deleteGroup(groupEntity: GroupEntity) {
        val updatedGroups = groups.value.toMutableList()
        updatedGroups.removeIf { it.id == groupEntity.id }
        groups.value = updatedGroups
    }
}