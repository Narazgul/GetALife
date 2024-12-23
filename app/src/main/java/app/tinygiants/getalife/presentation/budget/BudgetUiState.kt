package app.tinygiants.getalife.presentation.budget

import androidx.compose.runtime.Immutable
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.presentation.shared_composables.UiText
import app.tinygiants.getalife.presentation.shared_composables.ErrorMessage

@Immutable
data class BudgetUiState(
    val bannerState: BannerUiState,
    val groups: Map<Group, List<Category>>,
    val isLoading: Boolean,
    val userMessage: UiText?,
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

sealed class BannerUiState(open val text: UiText) {
    data class AllAssigned(override val text: UiText): BannerUiState(text)
    data class AssignableMoneyAvailable(override val text: UiText): BannerUiState(text)
    data class OverDistributed(override val text: UiText): BannerUiState(text)
    data class Overspent(override val text: UiText): BannerUiState(text)
}