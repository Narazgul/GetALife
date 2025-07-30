package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.GroupDao
import app.tinygiants.getalife.data.local.entities.GroupEntity
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao,
    private val categoryRepository: CategoryRepository
) : GroupRepository {

    override fun getGroupsFlow(): Flow<List<Group>> =
        groupDao.getGroupsFlow()
            .map { entities -> entities.map { it.toDomain() } }

    override suspend fun addGroup(group: Group) =
        groupDao.addGroup(GroupEntity.fromDomain(group))

    override suspend fun updateGroup(group: Group) =
        groupDao.updateGroup(GroupEntity.fromDomain(group))

    override suspend fun deleteGroup(group: Group) =
        groupDao.deleteGroup(GroupEntity.fromDomain(group))

    override suspend fun getGroupsWithCategories(): Map<Group, List<Category>> {
        val groups = groupDao.getGroupsFlow().first().map { it.toDomain() }
        val groupsWithCategories = mutableMapOf<Group, List<Category>>()

        for (group in groups) {
            val categories = categoryRepository.getCategoriesInGroup(group.id)
            groupsWithCategories[group] = categories
        }

        return groupsWithCategories
    }
}