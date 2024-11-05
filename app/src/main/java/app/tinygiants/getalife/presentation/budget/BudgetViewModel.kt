package app.tinygiants.getalife.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.usecase.account.GetAssignableMoneySumUseCase
import app.tinygiants.getalife.domain.usecase.categories.GetCategoriesInGroupsUseCase
import app.tinygiants.getalife.domain.usecase.categories.category.AddCategoryUseCase
import app.tinygiants.getalife.domain.usecase.categories.category.DeleteCategoryUseCase
import app.tinygiants.getalife.domain.usecase.categories.category.UpdateCategoryUseCase
import app.tinygiants.getalife.domain.usecase.categories.group.AddGroupUseCase
import app.tinygiants.getalife.domain.usecase.categories.group.DeleteGroupUseCase
import app.tinygiants.getalife.domain.usecase.categories.group.UpdateGroupUseCase
import app.tinygiants.getalife.presentation.UiText
import app.tinygiants.getalife.presentation.budget.UserClickEvent.AddCategory
import app.tinygiants.getalife.presentation.budget.UserClickEvent.AddGroup
import app.tinygiants.getalife.presentation.budget.UserClickEvent.DeleteCategory
import app.tinygiants.getalife.presentation.budget.UserClickEvent.DeleteGroup
import app.tinygiants.getalife.presentation.budget.UserClickEvent.UpdateCategory
import app.tinygiants.getalife.presentation.budget.UserClickEvent.UpdateGroup
import app.tinygiants.getalife.presentation.shared_composables.ErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val getBudget: GetCategoriesInGroupsUseCase,
    private val getAssignableMoney: GetAssignableMoneySumUseCase,
    private val addGroup: AddGroupUseCase,
    private val updateGroup: UpdateGroupUseCase,
    private val deleteGroup: DeleteGroupUseCase,
    private val addCategory: AddCategoryUseCase,
    private val updateCategory: UpdateCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        BudgetUiState(
            assignableMoney = null,
            groups = emptyMap(),
            isLoading = true,
            errorMessage = null
        )
    )
    val uiState = _uiState.asStateFlow()

    // region Init

    init {
        loadBudget()
    }

    private fun loadBudget() {
        viewModelScope.launch {

            launch {
                getBudget()
                    .catch { throwable -> displayErrorState(throwable) }
                    .collect { result ->
                        result.onSuccess { budgetListElements -> displayBudgetList(budgetListElements) }
                        result.onFailure { throwable -> displayErrorState(throwable) }
                    }
            }

            launch {
                getAssignableMoney()
                    .catch { displayAssignableMoneyErrorState() }
                    .collect { result ->
                        result.onSuccess { assignableMoney -> displayAssignableMoney(assignableMoney) }
                        result.onFailure { displayAssignableMoneyErrorState() } }
            }

        }
    }

    // endregion

    // region User interaction

    fun onUserClickEvent(clickEvent: UserClickEvent) {
        viewModelScope.launch {
            when (clickEvent) {

                is AddGroup -> addGroup(groupName = clickEvent.name)
                is UpdateGroup -> updateGroup(group = clickEvent.group)
                is DeleteGroup -> deleteGroup(group = clickEvent.group)

                is AddCategory -> addCategory(groupId = clickEvent.groupId, categoryName = clickEvent.categoryName)
                is UpdateCategory -> updateCategory(category = clickEvent.category)
                is DeleteCategory -> deleteCategory(category = clickEvent.category)
            }
        }
    }

    // endregion

    // region Private Helper functions

    private fun displayBudgetList(groups: Map<Group, List<Category>>) {
        _uiState.update { budgetUiState ->
            budgetUiState.copy(
                groups = groups,
                isLoading = false
            )
        }
    }

    private fun displayAssignableMoney(assignableMoney: Money) {
        _uiState.update { budgetUiState ->
            budgetUiState.copy(assignableMoney = assignableMoney)
        }
    }

    private fun displayErrorState(exception: Throwable?) {
        _uiState.update { budgetUiState ->
            budgetUiState.copy(
                isLoading = false,
                errorMessage = ErrorMessage(
                    title = UiText.StringResource(resId = R.string.error_title),
                    subtitle = if (exception?.message != null) UiText.DynamicString(value = exception.message!!)
                    else UiText.StringResource(R.string.error_subtitle)
                )
            )
        }
    }

    private fun displayAssignableMoneyErrorState() {
        _uiState.update { budgetUiState ->
            budgetUiState.copy(
                assignableMoney = null,
                errorMessage = ErrorMessage(
                    title = UiText.StringResource(resId = R.string.error_title),
                    subtitle = UiText.StringResource(R.string.error_subtitle)
                )
            )
        }
    }

    // endregion
}