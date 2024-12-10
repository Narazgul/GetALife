package app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.group

import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.repository.GroupRepository
import javax.inject.Inject

class UpdateGroupUseCase @Inject constructor(private val repository: GroupRepository) {

    suspend operator fun invoke(group: Group) =
        repository.updateGroup(group = group)
}