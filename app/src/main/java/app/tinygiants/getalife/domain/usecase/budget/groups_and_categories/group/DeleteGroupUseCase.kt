package app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.group

import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.DeleteCategoryUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.group.DeleteGroupStatus.GroupHasCategoriesException
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.group.DeleteGroupStatus.SuccessfullyDeleted
import javax.inject.Inject

sealed class DeleteGroupStatus {
    data object SuccessfullyDeleted : DeleteGroupStatus()
    data class GroupHasCategoriesException(override val message: String) : Exception(message)
}

class DeleteGroupUseCase @Inject constructor(
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val groupRepository: GroupRepository,
    private val categoryRepository: CategoryRepository
) {

    suspend operator fun invoke(group: Group): Result<DeleteGroupStatus> {

        val categories = categoryRepository.getCategoriesInGroup(groupId = group.id)

        val hasNonInitialCategories = categories.any { !it.isInitialCategory }
        if (hasNonInitialCategories) return Result.failure(GroupHasCategoriesException("Group still has categories"))

        categories.find { it.isInitialCategory }?.let { initialCategory ->
            deleteCategoryUseCase(initialCategory)
        }

        groupRepository.deleteGroup(group)
        return Result.success(SuccessfullyDeleted)
    }
}