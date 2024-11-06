package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.data.local.entities.GroupEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GroupRepositoryFake : GroupRepository {

    val groupFlow = MutableStateFlow<List<GroupEntity>>(emptyList())

    override fun getGroupsFlow(): Flow<List<GroupEntity>> = groupFlow.asStateFlow()

    override suspend fun addGroup(groupEntity: GroupEntity) { groupFlow.value += groupEntity }

    override suspend fun updateGroup(groupEntity: GroupEntity) {
        groupFlow.value = groupFlow.value.map {
            if (it.id == groupEntity.id) groupEntity else it
        }
    }

    override suspend fun deleteGroup(groupEntity: GroupEntity) {
        groupFlow.value = groupFlow.value.filterNot { it.id == groupEntity.id }
    }
}