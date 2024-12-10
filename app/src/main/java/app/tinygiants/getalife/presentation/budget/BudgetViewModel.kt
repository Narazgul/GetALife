package app.tinygiants.getalife.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.UserHint
import app.tinygiants.getalife.domain.usecase.budget.GetAssignableMoneyUseCase
import app.tinygiants.getalife.domain.usecase.budget.GetBudgetUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.AddCategoryUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.DeleteCategoryUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.UpdateCategoryUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.group.AddGroupUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.group.DeleteGroupUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.group.UpdateGroupUseCase
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
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase
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
                            displayAssignableMoney(assignableMoney, overspentCategoryText)
                        }
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
                is UpdateCategory -> updateCategory(updatedCategory = clickEvent.category)
                is DeleteCategory -> deleteCategory(category = clickEvent.category)
            }
        }
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

    private fun displayErrorState(throwable: Throwable?) {
        if (throwable != null) Firebase.crashlytics.recordException(throwable)

        _uiState.update { budgetUiState ->
            budgetUiState.copy(
                isLoading = false,
                errorMessage = ErrorMessage(
                    title = StringResource(resId = R.string.error_title),
                    subtitle = if (throwable?.message != null) DynamicString(value = throwable.message!!)
                    else StringResource(R.string.error_subtitle)
                )
            )
        }
    }

    private fun displayAssignableMoneyErrorState(throwable: Throwable) {
        Firebase.crashlytics.recordException(throwable)

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