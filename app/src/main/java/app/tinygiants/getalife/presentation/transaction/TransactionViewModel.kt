package app.tinygiants.getalife.presentation.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.usecase.transaction.GetTransactionsUseCase
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
    private val getTransactions: GetTransactionsUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow(
        TransactionUiState(
            title = "TransactionScreen",
            transactions = emptyList(),
            isLoading = true,
            errorMessage = null
        )
    )
    val uiState = _uiState.asStateFlow()

    // region Init

    init {
        loadTransactions()
    }

    private fun loadTransactions() {
        viewModelScope.launch {

            launch {
                getTransactions()
                    .catch { throwable -> displayErrorState(throwable) }
                    .collect { result ->
                        result.onSuccess { transactions -> displayTransactions(transactions) }
                        result.onFailure { throwable -> displayErrorState(throwable) }
                    }
            }
        }
    }

    // endregion

    // region Private Helper functions

    private fun displayTransactions(transactions: List<Transaction>) {
        _uiState.update { transactionUiState ->
            transactionUiState.copy(
                transactions = transactions,
                isLoading = false,
                errorMessage = null
            )
        }
    }

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