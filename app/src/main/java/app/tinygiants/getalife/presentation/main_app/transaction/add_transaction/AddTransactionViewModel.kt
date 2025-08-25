package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.model.RecurrenceFrequency
import app.tinygiants.getalife.domain.model.categorization.CategoryMatch
import app.tinygiants.getalife.domain.model.categorization.CategorizationResult
import app.tinygiants.getalife.domain.model.categorization.NewCategorySuggestion
import app.tinygiants.getalife.domain.usecase.account.GetAccountsUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.GetCategoriesUseCase
import app.tinygiants.getalife.domain.usecase.categorization.SmartTransactionCategorizerUseCase
import app.tinygiants.getalife.domain.usecase.transaction.AddTransactionUseCase
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
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
    private val transactionRepository: TransactionRepository,
    private val smartCategorizer: SmartTransactionCategorizerUseCase,
    private val categoryRepository: CategoryRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState(categories = emptyList(), accounts = emptyList()))
    val uiState = _uiState.asStateFlow()

    // Recent transaction partners for Auto-Complete
    private val _partners = MutableStateFlow<List<String>>(emptyList())
    val partners = _partners.asStateFlow()

    // Smart categorization state
    private val _smartCategorizationState = MutableStateFlow(SmartCategorizationUiState())
    val smartCategorizationState = _smartCategorizationState.asStateFlow()

    // Transaction input fields for smart categorization
    private val _transactionPartner = MutableStateFlow("")
    private val _transactionDescription = MutableStateFlow("")
    private val _transactionAmount = MutableStateFlow(Money(0.0))

    // region Init

    init {
        loadCategories()
        loadAccounts()
        setupSmartCategorization()

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

    @OptIn(FlowPreview::class)
    private fun setupSmartCategorization() {
        viewModelScope.launch {
            // Monitor transaction input changes for smart categorization
            kotlinx.coroutines.flow.combine(
                _transactionPartner,
                _transactionDescription,
                _transactionAmount
            ) { partner, description, amount ->
                Triple(partner, description, amount)
            }
                .debounce(500) // Wait 500ms after user stops typing
                .filter { (partner, _, _) -> partner.length >= 3 } // Only trigger for meaningful input
                .distinctUntilChanged()
                .collect { (partner, description, amount) ->
                    suggestCategoryForTransaction(partner, description, amount)
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

    // region Smart Categorization

    /**
     * Update transaction partner input and trigger smart categorization
     */
    fun updateTransactionPartner(partner: String) {
        _transactionPartner.value = partner
    }

    /**
     * Update transaction description input and trigger smart categorization
     */
    fun updateTransactionDescription(description: String) {
        _transactionDescription.value = description
    }

    /**
     * Update transaction amount input and trigger smart categorization
     */
    fun updateTransactionAmount(amount: Money) {
        _transactionAmount.value = amount
    }

    /**
     * Manually trigger smart categorization suggestion
     */
    private suspend fun suggestCategoryForTransaction(partner: String, description: String, amount: Money) {
        if (partner.isBlank()) return

        _smartCategorizationState.update { it.copy(isLoading = true, error = null) }

        try {
            val result = smartCategorizer(partner, description, amount)

            _smartCategorizationState.update {
                it.copy(
                    isLoading = false,
                    categorizationResult = result,
                    showBottomSheet = result.hasAnyMatch,
                    error = null
                )
            }
        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
            _smartCategorizationState.update {
                it.copy(
                    isLoading = false,
                    error = "Kategorievorschlag fehlgeschlagen",
                    showBottomSheet = false
                )
            }
        }
    }

    /**
     * User accepted a category match suggestion
     */
    fun onCategorySuggestionAccepted(categoryId: Long) {
        viewModelScope.launch {
            try {
                val category = categoryRepository.getCategoryById(categoryId)
                category?.let {
                    // Update UI state with selected category
                    _uiState.update { it.copy(selectedCategory = category) }
                }
                dismissCategorizationBottomSheet()
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
            }
        }
    }

    /**
     * User wants to create a new category from AI suggestion
     */
    fun onCreateCategoryFromSuggestion(suggestion: NewCategorySuggestion) {
        viewModelScope.launch {
            try {
                // Find the target group
                val group = groupRepository.getAllGroups().find { it.id == suggestion.groupId }
                    ?: return@launch

                // Create new category
                val newCategory = Category(
                    id = 0, // Will be auto-generated
                    groupId = suggestion.groupId,
                    emoji = suggestion.emoji,
                    name = suggestion.categoryName,
                    budgetTarget = suggestion.suggestedBudget ?: Money(0.0),
                    monthlyTargetAmount = null,
                    targetMonthsRemaining = null,
                    listPosition = 999, // Add at end
                    isInitialCategory = false,
                    linkedAccountId = null,
                    updatedAt = Clock.System.now(),
                    createdAt = Clock.System.now()
                )

                categoryRepository.addCategory(newCategory)

                // Update UI state with new category
                _uiState.update {
                    it.copy(
                        categories = it.categories + newCategory,
                        selectedCategory = newCategory
                    )
                }

                dismissCategorizationBottomSheet()
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
                _smartCategorizationState.update {
                    it.copy(error = "Kategorie konnte nicht erstellt werden")
                }
            }
        }
    }

    /**
     * Dismiss the smart categorization bottom sheet
     */
    fun dismissCategorizationBottomSheet() {
        _smartCategorizationState.update {
            it.copy(showBottomSheet = false)
        }
    }

    /**
     * Clear smart categorization error
     */
    fun clearCategorizationError() {
        _smartCategorizationState.update {
            it.copy(error = null)
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