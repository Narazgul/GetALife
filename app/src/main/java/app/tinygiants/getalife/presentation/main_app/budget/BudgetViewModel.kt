package app.tinygiants.getalife.presentation.main_app.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.UserHint
import app.tinygiants.getalife.domain.usecase.budget.AssignableMoneyException
import app.tinygiants.getalife.domain.usecase.budget.GetAssignableMoneyUseCase
import app.tinygiants.getalife.domain.usecase.budget.GetBudgetUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.AddCategoryUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.DeleteCategoryStatus.CategoryHasTransactionsException
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.DeleteCategoryUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.UpdateCategoryUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.group.AddGroupUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.group.DeleteGroupStatus.GroupHasCategoriesException
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.group.DeleteGroupUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.group.UpdateGroupUseCase
import app.tinygiants.getalife.presentation.main_app.budget.BannerUiState.AllAssigned
import app.tinygiants.getalife.presentation.main_app.budget.BannerUiState.AssignableMoneyAvailable
import app.tinygiants.getalife.presentation.main_app.budget.BannerUiState.OverDistributed
import app.tinygiants.getalife.presentation.main_app.budget.BannerUiState.Overspent
import app.tinygiants.getalife.presentation.main_app.budget.UserClickEvent.AddCategory
import app.tinygiants.getalife.presentation.main_app.budget.UserClickEvent.AddGroup
import app.tinygiants.getalife.presentation.main_app.budget.UserClickEvent.DeleteCategory
import app.tinygiants.getalife.presentation.main_app.budget.UserClickEvent.DeleteGroup
import app.tinygiants.getalife.presentation.main_app.budget.UserClickEvent.UpdateCategory
import app.tinygiants.getalife.presentation.main_app.budget.UserClickEvent.UpdateGroup
import app.tinygiants.getalife.presentation.shared_composables.ErrorMessage
import app.tinygiants.getalife.presentation.shared_composables.UiText
import app.tinygiants.getalife.presentation.shared_composables.UiText.DynamicString
import app.tinygiants.getalife.presentation.shared_composables.UiText.StringResource
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val getBudget: GetBudgetUseCase,
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
            userMessage = null,
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
                    .catch { throwable ->
                        Firebase.crashlytics.recordException(throwable)
                        displayError(throwable)
                    }
                    .collect { result ->
                        result.onSuccess { budgetListElements -> displayBudgetList(budgetListElements) }
                        result.onFailure { throwable ->
                            Firebase.crashlytics.recordException(throwable)
                            displayError(throwable)
                        }
                    }
            }

            launch {
                getAssignableMoney()
                    .catch { throwable ->
                        Firebase.crashlytics.recordException(throwable)
                        displayError(throwable as AssignableMoneyException)
                    }
                    .collect { result ->
                        result.onSuccess { (assignableMoney, overspentCategoryText) ->
                            displayAssignableMoney(assignableMoney, overspentCategoryText)
                        }
                        result.onFailure { throwable ->
                            Firebase.crashlytics.recordException(throwable)
                            displayError(throwable)
                        }
                    }
            }
        }
    }

    // endregion

    // region Interaction from UI

    fun onUserClickEvent(clickEvent: UserClickEvent) {
        viewModelScope.launch {
            when (clickEvent) {

                is AddGroup -> addGroup(groupName = clickEvent.name)
                is UpdateGroup -> updateGroup(group = clickEvent.group)
                is DeleteGroup -> deleteGroup(group = clickEvent.group)
                    .onFailure { throwable -> displayUserMessage(throwable) }

                is AddCategory -> addCategory(groupId = clickEvent.groupId, categoryName = clickEvent.categoryName)
                is UpdateCategory -> updateCategory(updatedCategory = clickEvent.category)
                is DeleteCategory -> deleteCategory(category = clickEvent.category)
                    .onFailure { throwable -> displayUserMessage(throwable) }
            }
        }
    }

    fun onUserMessageShown() = _uiState.update { budgetUiState ->
        budgetUiState.copy(userMessage = null)
    }

    // endregion

    // region Private Helper functions

    private fun displayBudgetList(groups: Map<Group, List<Category>>) {
        _uiState.update { budgetUiState ->
            val groupsWithOptionalText = addOptionalString(groups)
            budgetUiState.copy(
                groups = groupsWithOptionalText,
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

    private fun displayUserMessage(throwable: Throwable) {

        val userMessage = when (throwable) {
            is CategoryHasTransactionsException -> StringResource(R.string.error_transactions_in_category)
            is GroupHasCategoriesException -> StringResource(R.string.error_categories_in_group)
            else -> DynamicString(value = "")
        }

        _uiState.update { budgetUiState ->
            budgetUiState.copy(userMessage = userMessage)
        }
    }

    private fun displayError(throwable: Throwable) {

        val errorMessage = ErrorMessage(
            title = StringResource(resId = R.string.error_title),
            subtitle = if (throwable.message != null) DynamicString(value = throwable.message ?: "")
            else StringResource(resId = R.string.error_subtitle)
        )

        when (throwable) {

            is AssignableMoneyException -> _uiState.update { budgetUiState ->
                budgetUiState.copy(
                    bannerState = AllAssigned(text = DynamicString("")),
                    errorMessage = errorMessage
                )
            }

            else -> _uiState.update { budgetUiState ->
                budgetUiState.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
            }
        }
    }

    private fun addOptionalString(groups: Map<Group, List<Category>>) = groups.mapValues { group ->
        group.value.map { category ->
            category.copy(optionalText = getOptionalTextStringResource(userHint = category.progress.userHint))
        }
    }

    private fun getOptionalTextStringResource(userHint: UserHint) = when (userHint) {
        UserHint.NoHint -> DynamicString(value = "")
        UserHint.AllSpent -> StringResource(R.string.all_spent)
        UserHint.FullyFunded -> StringResource(R.string.fully_funded)
        is UserHint.AssignMoreOrRemoveSpending -> StringResource(R.string.assign_more_or_remove_spending, userHint.amount)
        is UserHint.ExtraMoney -> StringResource(R.string.enjoy_your_extra_money, userHint.amount)
        is UserHint.MoreNeedForBudgetTarget -> StringResource(R.string.more_needed_to_reach_budget_target, userHint.amount)
        is UserHint.Spent -> StringResource(R.string.amount_spent, userHint.amount)
        is UserHint.SpentMoreThanAvailable -> StringResource(R.string.spent_more_than_available, userHint.amount)
    }

    private fun getBannerState(assignableMoney: Money, overspentCategoryText: UiText?): BannerUiState {

        if (assignableMoney == EmptyMoney() && overspentCategoryText != null) return Overspent(overspentCategoryText)

        return when {
            assignableMoney == EmptyMoney() -> AllAssigned(StringResource(resId = R.string.everything_distributed))
            assignableMoney > EmptyMoney() -> AssignableMoneyAvailable(
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

// endregion

}