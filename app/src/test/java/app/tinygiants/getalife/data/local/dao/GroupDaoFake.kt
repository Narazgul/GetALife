package app.tinygiants.getalife.data.local.dao

import app.tinygiants.getalife.data.local.entities.GroupEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class GroupDaoFake : GroupDao {

    private val groupsFlow = MutableStateFlow<List<GroupEntity>>(emptyList())

    override fun getGroupsFlow(): Flow<List<GroupEntity>> = groupsFlow

    override fun getGroups(): List<GroupEntity> = groupsFlow.value

    override suspend fun addGroup(groupEntity: GroupEntity) {
        val updatedGroups = groupsFlow.value.toMutableList()
        updatedGroups.add(groupEntity)
        groupsFlow.value = updatedGroups
    }

    override suspend fun updateGroup(groupEntity: GroupEntity) {
        val updatedGroups = groupsFlow.value.toMutableList()
        val index = updatedGroups.indexOfFirst { it.id == groupEntity.id }
        if (index != -1) {
            updatedGroups[index] = groupEntity
            groupsFlow.value = updatedGroups
        }
    }

    override suspend fun deleteGroup(groupEntity: GroupEntity) {
        val updatedGroups = groupsFlow.value.toMutableList()
        updatedGroups.removeIf { it.id == groupEntity.id }
        groupsFlow.value = updatedGroups
    }
}