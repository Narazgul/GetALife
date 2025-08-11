package app.tinygiants.getalife.presentation.main_app.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.usecase.account.AddAccountUseCase
import app.tinygiants.getalife.domain.usecase.account.DeleteAccountStatus
import app.tinygiants.getalife.domain.usecase.account.DeleteAccountUseCase
import app.tinygiants.getalife.domain.usecase.account.GetAccountsUseCase
import app.tinygiants.getalife.domain.usecase.account.UpdateAccountUseCase
import app.tinygiants.getalife.domain.usecase.budget.BudgetSelectionUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.GetCategoriesUseCase
import app.tinygiants.getalife.domain.usecase.transaction.TransferBetweenAccountsUseCase
import app.tinygiants.getalife.presentation.shared_composables.ErrorMessage
import app.tinygiants.getalife.presentation.shared_composables.UiText
import app.tinygiants.getalife.presentation.shared_composables.UiText.StringResource
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val getAccounts: GetAccountsUseCase,
    private val getCategories: GetCategoriesUseCase,
    private val addAccount: AddAccountUseCase,
    private val updateAccount: UpdateAccountUseCase,
    private val deleteAccount: DeleteAccountUseCase,
    private val transferBetweenAccounts: TransferBetweenAccountsUseCase,
    private val budgetSelectionUseCase: BudgetSelectionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AccountUiState(
            accounts = emptyList(),
            categories = emptyList(),
            selectedBudget = null,
            availableBudgets = emptyList(),
            isLoading = true,
            isSyncLoading = false,
            userMessage = null,
            errorMessage = null
        )
    )
    val uiState = _uiState.asStateFlow()

    // region Init

    init {
        initializeBudgetAndLoadData()
    }

    private fun initializeBudgetAndLoadData() {
        viewModelScope.launch {
            // Initialize default budget if needed
            try {
                budgetSelectionUseCase.initializeDefaultBudget()
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
            }

            // Observe budget changes and reload data accordingly
            launch {
                combine(
                    budgetSelectionUseCase.getBudgetsFlow(),
                    budgetSelectionUseCase.activeBudgetIdFlow
                ) { budgets, activeBudgetId ->
                    Pair(budgets, budgets.find { it.id == activeBudgetId })
                }.collect { (budgets, selectedBudget) ->
                    _uiState.update { state ->
                        state.copy(
                            availableBudgets = budgets,
                            selectedBudget = selectedBudget
                        )
                    }
                    
                    if (selectedBudget != null) {
                        loadAccountsAndCategories()
                    }
                }
            }
        }
    }

    private fun loadAccountsAndCategories() {
        loadAccounts()
        loadCategories()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            getAccounts()
                .catch { throwable ->
                    Firebase.crashlytics.recordException(throwable)
                    displayErrorState(throwable)
                }
                .collect { result ->
                    result.onSuccess { accounts -> displayAccounts(accounts) }
                    result.onFailure { throwable ->
                        Firebase.crashlytics.recordException(throwable)
                        displayErrorState(throwable)
                    }
                }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategories()
                .catch { throwable ->
                    Firebase.crashlytics.recordException(throwable)
                    displayErrorState(throwable)
                }
                .collect { categories -> displayCategories(categories) }
        }
    }

    // endregion

    // region Interaction from UI

    fun onUserClickEvent(clickEvent: UserClickEvent) {
        viewModelScope.launch {
            when (clickEvent) {
                is UserClickEvent.AddAccount -> addAccount(
                    name = clickEvent.name,
                    balance = clickEvent.balance,
                    type = clickEvent.type,
                    startingBalanceName = clickEvent.startingBalanceName,
                    startingBalanceDescription = clickEvent.startingBalanceDescription
                )

                is UserClickEvent.UpdateAccount -> updateAccount(account = clickEvent.account)
                is UserClickEvent.DeleteAccount -> {
                    deleteAccount(account = clickEvent.account)
                        .onSuccess { status ->
                            when (status) {
                                is DeleteAccountStatus.SuccessfullyDeleted -> {
                                    _uiState.update { state ->
                                        state.copy(userMessage = UiText.DynamicString("Account deleted successfully"))
                                    }
                                }
                                is DeleteAccountStatus.AccountClosedInsteadOfDeleted -> {
                                    _uiState.update { state ->
                                        state.copy(userMessage = StringResource(R.string.account_closed_instead_of_deleted))
                                    }
                                }
                            }
                        }
                        .onFailure { throwable ->
                            displayUserMessage(throwable)
                        }
                }
                is UserClickEvent.TransferBetweenAccounts -> {
                    try {
                        transferBetweenAccounts(
                            fromAccount = clickEvent.fromAccount,
                            toAccount = clickEvent.toAccount,
                            amount = clickEvent.amount,
                            description = clickEvent.description
                        )
                        _uiState.update { state ->
                            state.copy(userMessage = UiText.DynamicString("Transfer successful"))
                        }
                    } catch (throwable: Throwable) {
                        Firebase.crashlytics.recordException(throwable)
                        displayUserMessage(throwable)
                    }
                }
                is UserClickEvent.BudgetSelected -> {
                    budgetSelectionUseCase.switchToBudget(clickEvent.budget.id)
                }
                is UserClickEvent.SyncRequested -> {
                    syncBudgets()
                }
            }
        }
    }

    private fun syncBudgets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncLoading = true) }
            try {
                // Sync is automatic with Firestore, but we can trigger repository sync
                budgetSelectionUseCase.getCurrentFirebaseUserId()
                // Repository sync logic will be handled automatically by Firestore
                _uiState.update { it.copy(isSyncLoading = false) }
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
                _uiState.update { 
                    it.copy(
                        isSyncLoading = false,
                        userMessage = UiText.DynamicString("Sync failed: ${e.message}")
                    )
                }
            }
        }
    }

    fun onUserMessageShown() = _uiState.update { accountUiState ->
        accountUiState.copy(userMessage = null)
    }

    // endregion

    // region Private Helper functions

    private fun displayAccounts(accounts: List<Account>) {
        _uiState.update { state ->
            state.copy(
                accounts = accounts,
                isLoading = false,
                errorMessage = null
            )
        }
    }

    private fun displayCategories(categories: List<Category>) {
        _uiState.update { state ->
            state.copy(categories = categories)
        }
    }

    private fun displayUserMessage(throwable: Throwable) {
        val message = when (throwable) {
            else -> StringResource(R.string.error_generic)
        }
        
        _uiState.update { accountUiState ->
            accountUiState.copy(userMessage = message)
        }
    }

    private fun displayErrorState(throwable: Throwable?) {
        _uiState.update {
            AccountUiState(
                accounts = emptyList(),
                categories = emptyList(),
                selectedBudget = null,
                availableBudgets = emptyList(),
                isLoading = false,
                isSyncLoading = false,
                userMessage = null,
                errorMessage = ErrorMessage(
                    title = StringResource(R.string.error_title),
                    subtitle = if (throwable?.message != null) UiText.DynamicString(throwable.message ?: "")
                    else StringResource(R.string.error_subtitle)
                )
            )
        }
    }

    // endregion
}