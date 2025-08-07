package app.tinygiants.getalife.data.repository

import app.tinygiants.getalife.data.local.dao.GroupDao
import app.tinygiants.getalife.data.local.entities.GroupEntity
import app.tinygiants.getalife.data.remote.FirestoreDataSource
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.usecase.GetCurrentBudgetUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Offline-first repository for Groups.
 * Room = Single Source of Truth, Firestore = automatic sync layer.
 */
class GroupRepositoryImpl @Inject constructor(
    private val groupDao: GroupDao,
    private val getCurrentBudget: GetCurrentBudgetUseCase,
    private val categoryRepository: CategoryRepository,
    private val firestore: FirestoreDataSource,
    private val externalScope: CoroutineScope
) : GroupRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getGroupsFlow(): Flow<List<Group>> =
        getCurrentBudget.currentBudgetIdOrDefaultFlow.flatMapLatest { budgetId ->
            groupDao.getGroupsFlow(budgetId).map { entities ->
                entities.map { entity -> entity.toDomain() }
            }
        }

    override suspend fun addGroup(group: Group) {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        val entity = GroupEntity.fromDomain(group, budgetId)
        groupDao.addGroup(entity.copy(isSynced = false))
        syncGroupInBackground(entity)
    }

    override suspend fun updateGroup(group: Group) {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        val entity = GroupEntity.fromDomain(group, budgetId)
        groupDao.updateGroup(entity.copy(isSynced = false))
        syncGroupInBackground(entity)
    }

    override suspend fun deleteGroup(group: Group) {
        val budgetId = getCurrentBudget.requireCurrentBudgetId()
        val entity = GroupEntity.fromDomain(group, budgetId)
        groupDao.deleteGroup(entity)
        // TODO: firestore deletion
    }

    private fun syncGroupInBackground(entity: GroupEntity) {
        externalScope.async {
            try {
                firestore.saveGroup(entity)
                // Mark as synced in local database
                groupDao.updateGroup(entity.copy(isSynced = true))
            } catch (_: Exception) {
                // Firestore handles offline persistence automatically
            }
        }
    }

    suspend fun syncWithFirestore(budgetId: String) {
        try {
            val remote = firestore.getGroups(budgetId)
            val local = groupDao.getGroupsFlow(budgetId).first()

            remote.forEach { remoteGroup ->
                val localGroup = local.find { it.id == remoteGroup.id }
                if (localGroup == null || !localGroup.isSynced) {
                    groupDao.updateGroup(remoteGroup.copy(isSynced = true))
                }
            }
        } catch (_: Exception) {
            // Handle gracefully - local remains source of truth
        }
    }

    override suspend fun getGroupsWithCategories(): Map<Group, List<Category>> {
        val groups = getGroupsFlow().first()
        return groups.associateWith { group ->
            categoryRepository.getCategoriesInGroup(group.id)
        }
    }

    override suspend fun getGroupByName(name: String): Group? {
        val groups = getGroupsFlow().first()
        return groups.firstOrNull { it.name == name }
    }
}