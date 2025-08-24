package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.model.RecurrenceFrequency
import app.tinygiants.getalife.domain.usecase.account.GetAccountsUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.GetCategoriesUseCase
import app.tinygiants.getalife.domain.usecase.transaction.AddTransactionUseCase
import app.tinygiants.getalife.domain.repository.TransactionRepository
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val getCategories: GetCategoriesUseCase,
    private val getAccounts: GetAccountsUseCase,
    private val addTransaction: AddTransactionUseCase,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState(categories = emptyList(), accounts = emptyList()))
    val uiState = _uiState.asStateFlow()

    // Recent transaction partners for Auto-Complete
    private val _partners = MutableStateFlow<List<String>>(emptyList())
    val partners = _partners.asStateFlow()

    // region Init

    init {
        loadCategories()
        loadAccounts()

        // Load recent partners once at startup
        viewModelScope.launch {
            transactionRepository.getTransactionsFlow()
                .catch { throwable -> Firebase.crashlytics.recordException(throwable) }
                .collect { transactions ->
                    val recentPartners = transactions
                        .map { it.transactionPartner }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .take(25)
                    _partners.value = recentPartners
                }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            launch {
                getCategories()
                    .catch { throwable -> Firebase.crashlytics.recordException(throwable) }
                    .collect { categories ->
                        _uiState.update { uiState -> uiState.copy(categories = categories) }
                    }
            }
        }
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            launch {
                getAccounts()
                    .catch { throwable -> Firebase.crashlytics.recordException(throwable) }
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
        description: String,
        dateOfTransaction: Instant = Clock.System.now(),
        recurrenceFrequency: RecurrenceFrequency? = null
    ) {
        viewModelScope.launch {
            addTransaction(
                accountId = accountId,
                category = category,
                amount = amount,
                direction = direction,
                transactionPartner = transactionPartner,
                description = description,
                dateOfTransaction = dateOfTransaction,
                recurrenceFrequency = recurrenceFrequency
            )
        }
    }

    // endregion
}