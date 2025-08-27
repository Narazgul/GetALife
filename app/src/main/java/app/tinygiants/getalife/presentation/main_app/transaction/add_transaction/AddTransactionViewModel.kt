package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.RecurrenceFrequency
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.model.categorization.NewCategorySuggestion
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.repository.TransactionRepository
import app.tinygiants.getalife.domain.usecase.OnboardingPrefsUseCase
import app.tinygiants.getalife.domain.usecase.account.GetAccountsUseCase
import app.tinygiants.getalife.domain.usecase.account.AddAccountUseCase
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.GetCategoriesUseCase
import app.tinygiants.getalife.domain.usecase.categorization.SmartTransactionCategorizerUseCase
import app.tinygiants.getalife.domain.usecase.transaction.AddTransactionUseCase
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant

sealed interface GuidedTransactionStep {
    data object Type : GuidedTransactionStep
    data object Amount : GuidedTransactionStep
    data object Account : GuidedTransactionStep
    data object ToAccount : GuidedTransactionStep // For transfers only
    data object Partner : GuidedTransactionStep
    data object Category : GuidedTransactionStep
    data object Date : GuidedTransactionStep
    data object Optional : GuidedTransactionStep
    data object Done : GuidedTransactionStep

    companion object {
        val entries = listOf(Type, Amount, Account, ToAccount, Partner, Category, Date, Optional, Done)
    }
}

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val getCategories: GetCategoriesUseCase,
    private val getAccounts: GetAccountsUseCase,
    private val addAccount: AddAccountUseCase,
    private val addTransaction: AddTransactionUseCase,
    private val transactionRepository: TransactionRepository,
    private val smartCategorizer: SmartTransactionCategorizerUseCase,
    private val categoryRepository: CategoryRepository,
    private val groupRepository: GroupRepository,
    private val onboardingPrefsUseCase: OnboardingPrefsUseCase
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

    val transactionPartner: StateFlow<String> get() = _transactionPartner

    // region Init

    init {
        loadCategories()
        loadAccounts()
        setupSmartCategorization()
        initializeUiMode()

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

    private fun initializeUiMode() {
        viewModelScope.launch {
            val isTransactionOnboardingCompleted = onboardingPrefsUseCase.isTransactionOnboardingCompletedFlow.first()
            _uiState.update {
                it.copy(
                    isGuidedMode = !isTransactionOnboardingCompleted,
                    guidedStep = if (isTransactionOnboardingCompleted) GuidedTransactionStep.Done else GuidedTransactionStep.Type
                )
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

    /**
     * Create a temporary new category for guided mode dialog (will be saved when transaction is saved)
     */
    fun onCreateNewCategory(categoryName: String) {
        viewModelScope.launch {
            try {
                // Get all groups to find a suitable group
                val groups = groupRepository.getAllGroups()
                val defaultGroup = groups.firstOrNull() // Use first available group or create logic for default group

                if (defaultGroup != null) {
                    // Create temporary category (not saved to repository yet)
                    val tempCategory = Category(
                        id = -System.currentTimeMillis(), // Use negative ID to indicate it's temporary
                        groupId = defaultGroup.id,
                        emoji = "📁", // Default emoji, can be updated later by AI
                        name = categoryName,
                        budgetTarget = Money(0.0),
                        monthlyTargetAmount = null,
                        targetMonthsRemaining = null,
                        listPosition = 999, // Add at end
                        isInitialCategory = false,
                        linkedAccountId = null,
                        updatedAt = Clock.System.now(),
                        createdAt = Clock.System.now()
                    )

                    // Update UI state with temporary category (will be saved when transaction is saved)
                    _uiState.update {
                        it.copy(
                            categories = it.categories + tempCategory,
                            selectedCategory = tempCategory
                        )
                    }

                    // Automatically proceed to next step after creating and selecting category
                    moveToNextStep()
                }
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
                // Could show error to user here
            }
        }
    }

    // region Smart Categorization

    /**
     * Create a new account (used in guided mode when user creates account in workflow)
     */
    fun onCreateNewAccount(
        name: String,
        initialBalance: Money = Money(0.0),
        accountType: AccountType = AccountType.Checking
    ) {
        viewModelScope.launch {
            try {
                // Create new account with AddAccountUseCase
                addAccount(
                    name = name,
                    balance = initialBalance,
                    type = accountType,
                    startingBalanceName = "Startsaldo",
                    startingBalanceDescription = "Anfangsbestand des Kontos"
                )

                // Small delay to ensure account is saved before reloading
                kotlinx.coroutines.delay(200)

                // Reload accounts after creation to get the new account
                getAccounts()
                    .catch { throwable -> Firebase.crashlytics.recordException(throwable) }
                    .collect { result ->
                        result.onSuccess { accounts ->
                            _uiState.update { uiState ->
                                uiState.copy(accounts = accounts)
                            }

                            // Auto-select the newly created account
                            val newAccount = accounts.find { it.name == name }
                            newAccount?.let { account ->
                                _uiState.update { it.copy(selectedAccount = account) }
                                // Automatically proceed to next step after selecting account
                                moveToNextStep()
                            }
                        }
                    }

            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
                // Could show error to user here
            }
        }
    }

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
                groupRepository.getAllGroups().find { it.id == suggestion.groupId }
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

    // region Guided Mode Functions

    fun moveToNextStep() {
        val currentStep = uiState.value.guidedStep
        val currentIndex = GuidedTransactionStep.entries.indexOf(currentStep)

        // Determine next step based on transaction type
        val nextStep = when {
            currentIndex >= GuidedTransactionStep.entries.size - 1 -> return // Already at last step

            // For transfers, after Account selection go to ToAccount
            uiState.value.selectedDirection == TransactionDirection.Unknown &&
                    currentStep == GuidedTransactionStep.Account -> GuidedTransactionStep.ToAccount

            // For transfers, skip Partner and Category steps after ToAccount
            uiState.value.selectedDirection == TransactionDirection.Unknown &&
                    currentStep == GuidedTransactionStep.ToAccount -> GuidedTransactionStep.Date

            // For Inflow and Outflow, skip ToAccount step entirely
            (uiState.value.selectedDirection == TransactionDirection.Inflow ||
                    uiState.value.selectedDirection == TransactionDirection.Outflow) &&
                    currentStep == GuidedTransactionStep.Account -> GuidedTransactionStep.Partner

            else -> GuidedTransactionStep.entries[currentIndex + 1]
        }

        _uiState.update { it.copy(guidedStep = nextStep) }
    }

    fun goToStep(step: GuidedTransactionStep) {
        _uiState.update { it.copy(guidedStep = step) }
    }

    fun onGuidedTransactionTypeSelected(direction: TransactionDirection) {
        _uiState.update { it.copy(selectedDirection = direction) }
        moveToNextStep()
    }

    fun onGuidedAmountEntered(amount: Money) {
        _uiState.update { it.copy(selectedAmount = amount) }
    }

    fun onGuidedAccountSelected(account: app.tinygiants.getalife.domain.model.Account) {
        _uiState.update { it.copy(selectedAccount = account) }
        moveToNextStep()
    }

    /**
     * Function to handle selection of destination account for transfers
     */
    fun onGuidedToAccountSelected(toAccount: app.tinygiants.getalife.domain.model.Account) {
        _uiState.update { it.copy(selectedToAccount = toAccount) }
        moveToNextStep()
    }

    fun onGuidedPartnerEntered(partner: String) {
        _uiState.update { it.copy(selectedPartner = partner) }
        updateTransactionPartner(partner) // Trigger smart categorization
    }

    fun onGuidedCategorySelected(category: Category?) {
        _uiState.update { it.copy(selectedCategory = category) }
        moveToNextStep()
    }

    fun onGuidedDateSelected(date: java.time.LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    fun onGuidedDescriptionChanged(description: String) {
        _uiState.update { it.copy(selectedDescription = description) }
    }

    fun onGuidedTransactionComplete() {
        val state = uiState.value
        viewModelScope.launch {
            // Save the transaction with all collected data
            state.selectedDirection?.let { direction ->
                state.selectedAmount?.let { amount ->
                    state.selectedAccount?.let { account ->

                        // If selected category has negative ID, it's temporary and needs to be saved first
                        val finalCategory = state.selectedCategory?.let { category ->
                            if (category.id < 0) {
                                // Create and save the new category
                                val newCategory = category.copy(id = 0) // Reset ID for auto-generation
                                categoryRepository.addCategory(newCategory)
                                // Return the category with ID 0, the repository will handle ID assignment
                                newCategory
                            } else {
                                category
                            }
                        }

                        addTransaction(
                            accountId = account.id,
                            category = finalCategory,
                            amount = amount,
                            direction = direction,
                            transactionPartner = state.selectedPartner,
                            description = state.selectedDescription,
                            dateOfTransaction = state.selectedDate?.let {
                                kotlin.time.Instant.fromEpochSeconds(
                                    it.toEpochDay() * 24 * 60 * 60
                                )
                            } ?: kotlin.time.Clock.System.now(),
                            recurrenceFrequency = null
                        )

                        // Mark transaction onboarding as completed
                        onboardingPrefsUseCase.markTransactionOnboardingCompleted()

                        // Move to Done step
                        _uiState.update { it.copy(guidedStep = GuidedTransactionStep.Done) }
                    }
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
            // If selected category has negative ID, it's temporary and needs to be saved first
            val finalCategory = category?.let { cat ->
                if (cat.id < 0) {
                    val newCategory = cat.copy(id = 0)
                    categoryRepository.addCategory(newCategory)
                    newCategory
                } else {
                    cat
                }
            }

            addTransaction(
                accountId = accountId,
                category = finalCategory,
                amount = amount,
                direction = direction,
                transactionPartner = transactionPartner,
                description = description,
                dateOfTransaction = dateOfTransaction,
                recurrenceFrequency = recurrenceFrequency
            )
            if (uiState.value.isGuidedMode) {
                onboardingPrefsUseCase.markTransactionOnboardingCompleted()
            }
        }
    }

    // endregion

    /**
     * Switch from guided mode to standard mode and reset transaction data for new transaction
     */
    fun switchToStandardMode() {
        _uiState.update {
            it.copy(
                isGuidedMode = false,
                guidedStep = GuidedTransactionStep.Done,
                // Clear transaction data for new transaction
                selectedDirection = null,
                selectedAmount = null,
                selectedAccount = null,
                selectedToAccount = null,
                selectedPartner = "",
                selectedCategory = null,
                selectedDate = null,
                selectedDescription = ""
            )
        }
    }

    /**
     * Clear current error state
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Retry the last failed action
     */
    fun retryLastAction() {
        // Clear error state - specific retry logic can be added later based on error type
        clearError()
    }

    /**
     * Validate transaction form and update validation errors
     */
    private fun validateTransactionForm() {
        val currentState = uiState.value
        val errors = mutableSetOf<ValidationError>()

        // Validate amount
        val amount = currentState.selectedAmount?.asDouble() ?: 0.0
        when {
            amount <= 0 -> errors.add(ValidationError.AMOUNT_ZERO_OR_NEGATIVE)
            amount > 999_999_999 -> errors.add(ValidationError.AMOUNT_TOO_LARGE)
        }

        // Validate partner
        when {
            currentState.selectedPartner.isBlank() -> errors.add(ValidationError.PARTNER_EMPTY)
            currentState.selectedPartner.length > 100 -> errors.add(ValidationError.PARTNER_TOO_LONG)
        }

        // Validate accounts
        if (currentState.selectedAccount == null) {
            errors.add(ValidationError.ACCOUNT_NOT_SELECTED)
        }

        // For transfers, validate destination account
        if (currentState.selectedDirection == TransactionDirection.Unknown) {
            when {
                currentState.selectedToAccount == null -> errors.add(ValidationError.TO_ACCOUNT_NOT_SELECTED)
                currentState.selectedAccount?.id == currentState.selectedToAccount?.id ->
                    errors.add(ValidationError.TO_ACCOUNT_SAME_AS_FROM)
            }
        }

        // Validate description length
        if (currentState.selectedDescription.length > 500) {
            errors.add(ValidationError.DESCRIPTION_TOO_LONG)
        }

        // Update state with validation errors
        _uiState.update {
            it.copy(
                validationErrors = errors,
                isFormValid = errors.isEmpty()
            )
        }
    }

    /**
     * Handle errors with appropriate user feedback
     */
    private fun handleError(throwable: Throwable, context: String = "") {
        val uiError = when (throwable) {
            is java.net.UnknownHostException,
            is java.net.ConnectException -> UiError.NetworkError("Keine Internetverbindung")

            is IllegalArgumentException -> UiError.ValidationError(context, throwable.message ?: "Ungültige Eingabe")
            else -> UiError.UnknownError(throwable.message ?: "Ein unbekannter Fehler ist aufgetreten")
        }

        _uiState.update { it.copy(error = uiError) }
        Firebase.crashlytics.recordException(throwable)
    }
}