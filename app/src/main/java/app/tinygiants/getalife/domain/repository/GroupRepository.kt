package app.tinygiants.getalife.domain.repository

import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Group
import kotlinx.coroutines.flow.Flow

interface GroupRepository {

    fun getGroupsFlow(): Flow<List<Group>>
    suspend fun addGroup(group: Group)
    suspend fun updateGroup(group: Group)
    suspend fun deleteGroup(group: Group)
    suspend fun getGroupsWithCategories(): Map<Group, List<Category>>

}