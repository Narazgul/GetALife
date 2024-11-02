package app.tinygiants.getalife.presentation.transaction

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.usecase.account.GetAccountsUseCase
import app.tinygiants.getalife.domain.usecase.categories.category.GetCategoriesUseCase
import app.tinygiants.getalife.domain.usecase.transaction.AddTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val getCategories: GetCategoriesUseCase,
    private val getAccounts: GetAccountsUseCase,
    private val addTransaction: AddTransactionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState(categories = emptyList(), accounts = emptyList()))
    val uiState = _uiState.asStateFlow()

    // region Init

    init {
        loadCategories()
        loadAccounts()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            launch {
                getCategories()
                    .catch { throwable -> Log.e("AddTransactionViewModel", "Error: ${throwable.message}") }
                    .collect { result ->
                        result.onSuccess { categories -> _uiState.update { uiState -> uiState.copy(categories = categories) } }
                    }
            }
        }
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            launch {
                getAccounts()
                    .catch { throwable -> Log.e("AddTransactionViewModel", "Error: ${throwable.message}") }
                    .collect { result ->
                        result.onSuccess { accounts -> _uiState.update { uiState -> uiState.copy(accounts = accounts) } }
                    }
            }
        }
    }

    // endregion

    // region User interaction

    fun onSaveTransactionClicked(
        amount: Money,
        direction: TransactionDirection,
        accountId: Long,
        category: Category?,
        transactionPartner: String,
        description: String
    ) {
        viewModelScope.launch {
            addTransaction(amount, direction, accountId, category, transactionPartner, description)
        }
    }

    // endregion
}