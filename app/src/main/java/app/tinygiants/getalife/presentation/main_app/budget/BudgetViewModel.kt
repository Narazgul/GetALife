package app.tinygiants.getalife.presentation.main_app.budget

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.BudgetMonth
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.usecase.budget.AssignableMoneyException
import app.tinygiants.getalife.domain.usecase.budget.CarryOverToNextMonthUseCase
import app.tinygiants.getalife.domain.usecase.budget.GetBudgetForMonthUseCase
import app.tinygiants.getalife.domain.usecase.budget.UpdateCategoryMonthlyStatusUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.AddCategoryUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.DeleteCategoryStatus
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.DeleteCategoryUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.UpdateCategoryUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.group.AddGroupUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.group.DeleteGroupStatus
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.group.DeleteGroupUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.group.UpdateGroupUseCase
import app.tinygiants.getalife.presentation.main_app.budget.BannerUiState.AllAssigned
import app.tinygiants.getalife.presentation.main_app.budget.UserClickEvent.AddCategory
import app.tinygiants.getalife.presentation.main_app.budget.UserClickEvent.AddGroup
import app.tinygiants.getalife.presentation.main_app.budget.UserClickEvent.DeleteCategory
import app.tinygiants.getalife.presentation.main_app.budget.UserClickEvent.DeleteGroup
import app.tinygiants.getalife.presentation.main_app.budget.UserClickEvent.UpdateCategory
import app.tinygiants.getalife.presentation.main_app.budget.UserClickEvent.UpdateCategoryAssignment
import app.tinygiants.getalife.presentation.main_app.budget.UserClickEvent.UpdateGroup
import app.tinygiants.getalife.presentation.shared_composables.ErrorMessage
import app.tinygiants.getalife.presentation.shared_composables.UiText.DynamicString
import app.tinygiants.getalife.presentation.shared_composables.UiText.StringResource
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import kotlin.time.Clock

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val getBudgetForMonth: GetBudgetForMonthUseCase,
    private val updateCategoryMonthlyStatus: UpdateCategoryMonthlyStatusUseCase,
    private val carryOverToNextMonth: CarryOverToNextMonthUseCase,
    private val addGroup: AddGroupUseCase,
    private val updateGroup: UpdateGroupUseCase,
    private val deleteGroup: DeleteGroupUseCase,
    private val addCategory: AddCategoryUseCase,
    private val updateCategory: UpdateCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase,
    private val getAccounts: app.tinygiants.getalife.domain.usecase.account.GetAccountsUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val currentMonthFlow = MutableStateFlow(
        savedStateHandle.get<String>("selectedMonth")?.let { YearMonth.parse(it) }
            ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let {
                YearMonth(it.year, it.month)
            }
    )
    val currentMonth = currentMonthFlow.asStateFlow()

    private val groupExpandStates = MutableStateFlow<Map<Long, Boolean>>(emptyMap())

    private val uiStateFlow = MutableStateFlow(
        BudgetUiState(
            bannerState = AllAssigned(text = DynamicString("")),
            groups = emptyMap(),
            creditCardAccountBalances = emptyMap(),
            isLoading = true,
            userMessage = null,
            errorMessage = null
        )
    )
    val uiState = uiStateFlow.asStateFlow()

    init {
        loadBudgetForMonth(currentMonthFlow.value)
    }

    private fun loadBudgetForMonth(yearMonth: YearMonth) {
        viewModelScope.launch {

            launch {
                getBudgetForMonth(yearMonth)
                    .catch { throwable ->
                        Firebase.crashlytics.recordException(throwable)
                        displayError(throwable)
                    }
                    .collect { result ->
                        result.onSuccess { budgetMonth -> displayBudgetMonth(budgetMonth) }
                        result.onFailure { throwable ->
                            Firebase.crashlytics.recordException(throwable)
                            displayError(throwable)
                        }
                    }
            }
        }
    }

    fun onUserClickEvent(clickEvent: UserClickEvent) {
        viewModelScope.launch {
            when (clickEvent) {
                is AddGroup -> runSuspendCatching({
                    addGroup(clickEvent.name)
                    loadBudgetForMonth(currentMonth.value)
                }, ::displayUserMessage)

                is UpdateGroup -> runSuspendCatching({
                    updateGroup(clickEvent.group)
                    updateGroupExpandCollapseState(clickEvent.group.id, clickEvent.group.isExpanded)
                    loadBudgetForMonth(currentMonth.value)
                }, ::displayUserMessage)
                is DeleteGroup -> {
                    deleteGroup(clickEvent.group)
                        .onSuccess { loadBudgetForMonth(currentMonth.value) }
                        .onFailure { displayUserMessage(it) }
                }
                is AddCategory -> runSuspendCatching({
                    addCategory(clickEvent.groupId, clickEvent.categoryName)
                    loadBudgetForMonth(currentMonth.value)
                }, ::displayUserMessage)

                is UpdateCategory -> runSuspendCatching({
                    updateCategory(clickEvent.category)
                    loadBudgetForMonth(currentMonth.value)
                }, ::displayUserMessage)
                is UpdateCategoryAssignment -> runSuspendCatching({
                    updateCategoryMonthlyStatus(clickEvent.categoryId, currentMonth.value, clickEvent.newAmount)
                    loadBudgetForMonth(currentMonth.value)
                }, ::displayUserMessage)
                is DeleteCategory -> {
                    deleteCategory(clickEvent.category)
                        .onSuccess { loadBudgetForMonth(currentMonth.value) }
                        .onFailure { displayUserMessage(it) }
                }
            }
        }
    }

    fun onUserMessageShown() = uiStateFlow.update { budgetUiState ->
        budgetUiState.copy(userMessage = null)
    }

    fun navigateToMonth(yearMonth: YearMonth) {
        currentMonthFlow.value = yearMonth
        savedStateHandle["selectedMonth"] = yearMonth.toString()
        loadBudgetForMonth(yearMonth)
    }

    private fun updateGroupExpandCollapseState(groupId: Long, isExpanded: Boolean) {
        groupExpandStates.update { currentExpandStates ->
            currentExpandStates.toMutableMap().apply {
                put(groupId, isExpanded)
            }
        }
    }

    private fun displayBudgetMonth(budgetMonth: BudgetMonth) {
        viewModelScope.launch {
            // Load account balances for credit card categories
            getAccounts().first().onSuccess { accounts ->
                val creditCardAccountBalances = extractCreditCardAccountBalances(budgetMonth, accounts)

                uiStateFlow.update { budgetUiState ->
                    val currentExpandStates = groupExpandStates.value

                    val groupsWithCategoryBudgets = budgetMonth.groups.mapKeys { (group, _) ->
                        val groupExpandedState = currentExpandStates[group.id] ?: group.isExpanded
                        group.copy(isExpanded = groupExpandedState)
                    }

                    val totalSpentMoney = budgetMonth.groups.values
                        .flatten()
                        .fold(Money()) { acc, categoryBudget -> acc + categoryBudget.spentAmount }

                    val bannerState = createBannerState(
                        assignableMoney = budgetMonth.totalAssignableMoney,
                        assignedMoney = budgetMonth.totalAssignedMoney,
                        spentMoney = totalSpentMoney
                    )

                    budgetUiState.copy(
                        bannerState = bannerState,
                        groups = groupsWithCategoryBudgets,
                        creditCardAccountBalances = creditCardAccountBalances,
                        isLoading = false
                    )
                }
            }.onFailure { throwable ->
                // Fallback: Continue without credit card account balances
                displayBudgetMonthFallback(budgetMonth)
            }
        }
    }

    private fun displayBudgetMonthFallback(budgetMonth: BudgetMonth) {
        uiStateFlow.update { budgetUiState ->
            val currentExpandStates = groupExpandStates.value

            val groupsWithCategoryBudgets = budgetMonth.groups.mapKeys { (group, _) ->
                val groupExpandedState = currentExpandStates[group.id] ?: group.isExpanded
                group.copy(isExpanded = groupExpandedState)
            }

            val totalSpentMoney = budgetMonth.groups.values
                .flatten()
                .fold(Money()) { acc, categoryBudget -> acc + categoryBudget.spentAmount }

            val bannerState = createBannerState(
                assignableMoney = budgetMonth.totalAssignableMoney,
                assignedMoney = budgetMonth.totalAssignedMoney,
                spentMoney = totalSpentMoney
            )

            budgetUiState.copy(
                bannerState = bannerState,
                groups = groupsWithCategoryBudgets,
                creditCardAccountBalances = emptyMap(),
                isLoading = false
            )
        }
    }

    /**
     * Extracts account balances for all credit card categories from the provided accounts.
     * Returns a map of linkedAccountId to account balance.
     */
    private fun extractCreditCardAccountBalances(
        budgetMonth: BudgetMonth,
        accounts: List<app.tinygiants.getalife.domain.model.Account>
    ): Map<Long, Money> {
        // Get all credit card categories with linkedAccountId
        val creditCardCategories = budgetMonth.groups.values
            .flatten()
            .filter { it.category.linkedAccountId != null }

        // Create lookup map from account ID to account balance
        val accountBalanceLookup = accounts.associate { account ->
            account.id to account.balance
        }

        // Map linkedAccountId to account balance for credit card categories
        return creditCardCategories.associate { categoryStatus ->
            val linkedAccountId = categoryStatus.category.linkedAccountId!!
            linkedAccountId to (accountBalanceLookup[linkedAccountId] ?: Money(0.0))
        }
    }

    private fun createBannerState(assignableMoney: Money, assignedMoney: Money, spentMoney: Money): BannerUiState {
        return when {
            spentMoney > assignedMoney -> {
                val overspentAmount = spentMoney - assignedMoney
                BannerUiState.Overspent(
                    StringResource(resId = R.string.spent_more_than_available, overspentAmount.formattedPositiveMoney)
                )
            }

            assignableMoney.asDouble() == 0.0 -> {
                AllAssigned(StringResource(resId = R.string.everything_distributed))
            }

            assignableMoney.asDouble() > 0.0 -> {
                BannerUiState.AssignableMoneyAvailable(
                    StringResource(resId = R.string.distribute_available_money, assignableMoney.formattedPositiveMoney)
                )
            }

            assignableMoney.asDouble() < 0.0 -> {
                BannerUiState.OverDistributed(
                    StringResource(resId = R.string.more_distributed_than_available, assignableMoney.formattedPositiveMoney)
                )
            }

            else -> {
                AllAssigned(StringResource(resId = R.string.everything_distributed))
            }
        }
    }

    private fun displayUserMessage(throwable: Throwable) {

        val userMessage = when (throwable) {
            is DeleteCategoryStatus.CategoryHasTransactionsException -> StringResource(R.string.error_transactions_in_category)
            is DeleteCategoryStatus.CreditCardCategoryCannotBeDeleted -> StringResource(R.string.credit_card_category_cannot_be_deleted)
            is DeleteGroupStatus.GroupHasCategoriesException -> StringResource(R.string.error_categories_in_group)
            else -> DynamicString(value = "")
        }

        uiStateFlow.update { budgetUiState ->
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

            is AssignableMoneyException -> uiStateFlow.update { budgetUiState ->
                budgetUiState.copy(
                    bannerState = AllAssigned(text = DynamicString("")),
                    errorMessage = errorMessage
                )
            }

            else -> uiStateFlow.update { budgetUiState ->
                budgetUiState.copy(
                    isLoading = false,
                    errorMessage = errorMessage
                )
            }
        }
    }
    
    private suspend inline fun <T> runSuspendCatching(
        block: suspend () -> T,
        onFailure: (Throwable) -> Unit
    ) {
        try {
            block()
        } catch (c: CancellationException) {
            throw c
        } catch (e: Exception) {
            onFailure(e)
        }
    }

}