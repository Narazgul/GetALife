package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.GroupDao
import app.tinygiants.getalife.data.local.entities.GroupEntity
import app.tinygiants.getalife.domain.repository.GroupRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(private val groupDao: GroupDao) : GroupRepository{

    override fun getGroupsFlow(): Flow<List<GroupEntity>> = groupDao.getGroupsFlow()
    override suspend fun getGroups() = groupDao.getGroups()
    override suspend fun addGroup(groupEntity: GroupEntity) = groupDao.addGroup(groupEntity = groupEntity)
    override suspend fun updateGroup(groupEntity: GroupEntity) = groupDao.updateGroup(groupEntity = groupEntity)
    override suspend fun deleteGroup(groupEntity: GroupEntity) = groupDao.deleteGroup(groupEntity = groupEntity)

}