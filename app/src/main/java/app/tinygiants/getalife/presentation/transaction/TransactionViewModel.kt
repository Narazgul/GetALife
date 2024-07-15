package app.tinygiants.getalife.presentation.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.usecase.account.GetAccountUseCase
import app.tinygiants.getalife.domain.usecase.account.GetAccountsUseCase
import app.tinygiants.getalife.domain.usecase.budget.category.GetCategoriesUseCase
import app.tinygiants.getalife.domain.usecase.transaction.DeleteTransactionUseCase
import app.tinygiants.getalife.domain.usecase.transaction.GetTransactionsForAccountUseCase
import app.tinygiants.getalife.domain.usecase.transaction.UpdateTransactionUseCase
import app.tinygiants.getalife.presentation.UiText
import app.tinygiants.getalife.presentation.composables.ErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val getTransactionsForAccount: GetTransactionsForAccountUseCase,
    private val updateTransaction: UpdateTransactionUseCase,
    private val deleteTransaction: DeleteTransactionUseCase,
    private val getAccount: GetAccountUseCase,
    private val getAccounts: GetAccountsUseCase,
    private val getCategories: GetCategoriesUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TransactionUiState(
            title = "",
            transactions = emptyList(),
            accounts = emptyList(),
            categories = emptyList(),
            isLoading = true,
            errorMessage = null
        )
    )
    val uiState = _uiState.asStateFlow()

    // region Init

    init {
        val accountId: Long? = savedStateHandle["accountId"]
        accountId?.let { loadTransactions(accountId) }
    }

    private fun loadTransactions(accountId: Long) {
        viewModelScope.launch {

            launch {
                getTransactionsForAccount(accountId = accountId)
                    .catch { throwable -> displayErrorState(throwable) }
                    .collect { result ->
                        result.onSuccess { transactions -> displayTransactions(transactions) }
                        result.onFailure { throwable -> displayErrorState(throwable) }
                    }
            }

            launch {
                getAccounts()
                    .catch { throwable -> displayErrorState(throwable) }
                    .collect { result ->
                        result.onSuccess { accounts -> listAvailableAccounts(accounts = accounts) }
                        result.onFailure { throwable -> displayErrorState(throwable) }
                    }
            }

            launch {
                val currentTransactionAccount = getAccount(accountId = accountId)
                displayAccountName(currentTransactionAccount)
            }

            launch {
                getCategories()
                    .catch { throwable -> displayErrorState(throwable) }
                    .collect { result ->
                        result.onSuccess { categories -> listAvailableCategories(categories = categories) }
                        result.onFailure { throwable -> displayErrorState(throwable) }
                    }
            }

        }
    }

    // endregion

    // region User interaction

    fun onUserClickEvent(clickEvent: UserClickEvent) {
        viewModelScope.launch {
            when (clickEvent) {
                is UserClickEvent.UpdateTransaction -> updateTransaction(transaction = clickEvent.transaction)
                is UserClickEvent.DeleteTransaction -> deleteTransaction(transaction = clickEvent.transaction)
            }
        }
    }

    // endregion

    // region Private Helper functions

    private fun displayAccountName(account: Account) = _uiState.update { uiState -> uiState.copy(title = account.name) }

    private fun displayTransactions(transactions: List<Transaction>) {
        _uiState.update { uiState ->
            uiState.copy(
                transactions = transactions,
                isLoading = false,
                errorMessage = null
            )
        }
    }

    private fun listAvailableAccounts(accounts: List<Account>) =
        _uiState.update { uiState -> uiState.copy(accounts = accounts) }

    private fun listAvailableCategories(categories: List<Category>) =
        _uiState.update { uiState -> uiState.copy(categories = categories) }

    private fun displayErrorState(exception: Throwable?) {
        _uiState.update { transactionUiState ->
            transactionUiState.copy(
                isLoading = false,
                errorMessage = ErrorMessage(
                    title = UiText.StringResource(resId = R.string.error_title),
                    subtitle = if (exception?.message != null) UiText.DynamicString(value = exception.message!!)
                    else UiText.StringResource(R.string.error_subtitle)
                )
            )
        }
    }

    // endregion
}