package app.tinygiants.getalife.domain.usecase.budget.group

import app.tinygiants.getalife.data.local.entities.GroupEntity
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.repository.GroupRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DeleteHeaderUseCase @Inject constructor(
    private val repository: GroupRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(group: Group) {

        val groupEntity = withContext(defaultDispatcher) {
            GroupEntity(
                id = group.id,
                name = group.name,
                listPosition = group.listPosition,
                isExpanded = group.isExpanded
            )
        }

        repository.deleteGroup(groupEntity = groupEntity)
    }
}