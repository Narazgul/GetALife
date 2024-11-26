package app.tinygiants.getalife.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.usecase.budget.GetAssignableMoneyUseCase
import app.tinygiants.getalife.domain.usecase.categories.GetCategoriesInGroupsUseCase
import app.tinygiants.getalife.domain.usecase.categories.category.AddCategoryUseCase
import app.tinygiants.getalife.domain.usecase.categories.category.DeleteCategoryUseCase
import app.tinygiants.getalife.domain.usecase.categories.category.UpdateCategoryUseCase
import app.tinygiants.getalife.domain.usecase.categories.group.AddGroupUseCase
import app.tinygiants.getalife.domain.usecase.categories.group.DeleteGroupUseCase
import app.tinygiants.getalife.domain.usecase.categories.group.UpdateGroupUseCase
import app.tinygiants.getalife.presentation.UiText
import app.tinygiants.getalife.presentation.UiText.DynamicString
import app.tinygiants.getalife.presentation.UiText.StringResource
import app.tinygiants.getalife.presentation.budget.BannerUiState.AllAssigned
import app.tinygiants.getalife.presentation.budget.BannerUiState.AssignableMoneyAvailable
import app.tinygiants.getalife.presentation.budget.BannerUiState.OverDistributed
import app.tinygiants.getalife.presentation.budget.BannerUiState.Overspent
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
    private val getAssignableMoney: GetAssignableMoneyUseCase,
    private val addGroup: AddGroupUseCase,
    private val updateGroup: UpdateGroupUseCase,
    private val deleteGroup: DeleteGroupUseCase,
    private val addCategory: AddCategoryUseCase,
    private val updateCategory: UpdateCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        BudgetUiState(
            bannerState = AllAssigned(text = DynamicString("")),
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
                    .catch { throwable -> displayAssignableMoneyErrorState(throwable) }
                    .collect { result ->
                        result.onSuccess { (assignableMoney, overspentCategoryText) ->
                            displayAssignableMoney(assignableMoney, overspentCategoryText) }
                        result.onFailure { throwable -> displayAssignableMoneyErrorState(throwable) }
                    }
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

    private fun displayAssignableMoney(assignableMoney: Money, overspentCategory: UiText?) {
        _uiState.update { budgetUiState ->
            budgetUiState.copy(
                bannerState = getBannerState(
                    assignableMoney = assignableMoney,
                    overspentCategoryText = overspentCategory
                )
            )
        }
    }

    private fun getBannerState(assignableMoney: Money, overspentCategoryText: UiText?): BannerUiState {

        if (assignableMoney.value == 0.0 && overspentCategoryText != null) return Overspent(overspentCategoryText)

        return when {
            assignableMoney.value == 0.0 -> AllAssigned(StringResource(resId = R.string.everything_distributed))
            assignableMoney.value > 0.0 -> AssignableMoneyAvailable(
                StringResource(
                    resId = R.string.distribute_available_money,
                    assignableMoney.formattedPositiveMoney
                )
            )
            else -> OverDistributed(
                StringResource(
                    resId = R.string.more_distributed_than_available,
                    assignableMoney.formattedPositiveMoney
                )
            )
        }
    }

    private fun displayErrorState(exception: Throwable?) {
        _uiState.update { budgetUiState ->
            budgetUiState.copy(
                isLoading = false,
                errorMessage = ErrorMessage(
                    title = StringResource(resId = R.string.error_title),
                    subtitle = if (exception?.message != null) DynamicString(value = exception.message!!)
                    else StringResource(R.string.error_subtitle)
                )
            )
        }
    }

    private fun displayAssignableMoneyErrorState(throwable: Throwable) {
        _uiState.update { budgetUiState ->
            val errorMessage = when (throwable) {
                is NoSuchElementException -> null
                else -> ErrorMessage(
                    title = StringResource(resId = R.string.error_title),
                    subtitle = StringResource(R.string.error_subtitle)
                )
            }

            budgetUiState.copy(
                bannerState = AllAssigned(text = DynamicString("")),
                errorMessage = errorMessage
            )
        }
    }

    // endregion
}