package app.tinygiants.getalife.presentation.budget

import androidx.compose.runtime.Immutable
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.presentation.shared_composables.ErrorMessage

@Immutable
data class BudgetUiState(
    val assignableMoney: Money?,
    val groups: Map<Group, List<Category>>,
    val isLoading: Boolean,
    val errorMessage: ErrorMessage?
)

sealed class UserClickEvent {
    data class AddGroup(val name: String) : UserClickEvent()
    data class UpdateGroup(val group: Group) : UserClickEvent()
    data class DeleteGroup(val group: Group) : UserClickEvent()

    data class AddCategory(val groupId: Long, val categoryName: String) : UserClickEvent()
    data class UpdateCategory(val category: Category) : UserClickEvent()
    data class DeleteCategory(val category: Category) : UserClickEvent()
}