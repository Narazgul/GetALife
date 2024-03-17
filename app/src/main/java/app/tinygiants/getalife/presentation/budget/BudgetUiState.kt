package app.tinygiants.getalife.presentation.budget

import androidx.compose.runtime.Immutable
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Header

@Immutable
data class BudgetUiState(
    val groups: Map<Header, List<Category>>,
    val isLoading: Boolean,
    val errorMessage: ErrorMessage?
)

@Immutable
data class ErrorMessage(
    val title: String?,
    val subtitle: String?
)

sealed class UserClickEvent {
    data class AddHeader(val name: String): UserClickEvent()
    data class UpdateHeader(val header: Header): UserClickEvent()
    data class DeleteHeader(val header: Header): UserClickEvent()

    data class AddCategory(val headerId: Long, val categoryName: String): UserClickEvent()
    data class UpdateCategory(val category: Category): UserClickEvent()
    data class DeleteCategory(val category: Category): UserClickEvent()
}