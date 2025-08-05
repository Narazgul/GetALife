package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Group
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GroupRepositoryFake : GroupRepository {

    val groupFlow = MutableStateFlow<List<Group>>(emptyList())

    override fun getGroupsFlow(): Flow<List<Group>> = groupFlow.asStateFlow()

    override suspend fun addGroup(group: Group) { groupFlow.value += group }

    override suspend fun updateGroup(group: Group) {
        groupFlow.value = groupFlow.value.map {
            if (it.id == group.id) group else it
        }
    }

    override suspend fun deleteGroup(group: Group) {
        groupFlow.value = groupFlow.value.filterNot { it.id == group.id }
    }

    override suspend fun getGroupsWithCategories(): Map<Group, List<Category>> {
        return groupFlow.value.associateWith { emptyList<Category>() }
    }

    override suspend fun getGroupByName(name: String): Group? {
        return groupFlow.value.firstOrNull { it.name == name }
    }
}