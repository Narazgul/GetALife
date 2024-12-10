package app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.group

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.di.Io
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.AddCategoryUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.random.Random

class AddGroupUseCase @Inject constructor(
    private val repository: GroupRepository,
    private val addCategory: AddCategoryUseCase,
    @Io private val ioDispatcher: CoroutineDispatcher,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    suspend operator fun invoke(groupName: String) {

        val groups = withContext(ioDispatcher) { repository.getGroupsFlow() }.first()

        withContext(defaultDispatcher) {

            val highestListPosition = groups.maxOfOrNull { group -> group.listPosition }
            val endOfListPosition = if (highestListPosition == null) 0 else highestListPosition + 1
            val trimmedGroupName = groupName.trim()

            val group = Group(
                id = Random.nextLong(),
                name = trimmedGroupName,
                sumOfAvailableMoney = EmptyMoney(),
                listPosition = endOfListPosition,
                isExpanded = true,
            )

            repository.addGroup(group = group)
            addCategory(groupId = group.id, categoryName = "", isInitialCategory = true)
        }
    }
}