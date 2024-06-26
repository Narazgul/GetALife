package app.tinygiants.getalife.presentation.budget

import androidx.compose.runtime.Immutable
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Header
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.presentation.composables.ErrorMessage

@Immutable
data class BudgetUiState(
    val assignableMoney: Money?,
    val groups: Map<Header, List<Category>>,
    val isLoading: Boolean,
    val errorMessage: ErrorMessage?
)

sealed class UserClickEvent {
    data class AddGroup(val name: String) : UserClickEvent()
    data class UpdateHeader(val header: Header) : UserClickEvent()
    data class DeleteHeader(val header: Header) : UserClickEvent()

    data class AddCategory(val headerId: Long, val categoryName: String) : UserClickEvent()
    data class UpdateAssignedMoney(val category: Category, val newAssignedMoney: Money): UserClickEvent()
    data class UpdateCategory(val category: Category) : UserClickEvent()
    data class DeleteCategory(val category: Category) : UserClickEvent()
}