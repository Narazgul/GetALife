package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.RecurrenceFrequency
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.usecase.OnboardingPrefsUseCase
import app.tinygiants.getalife.domain.usecase.account.AddAccountUseCase
import app.tinygiants.getalife.domain.usecase.account.GetAccountsUseCase
import app.tinygiants.getalife.domain.usecase.budget.groups_and_categories.category.GetCategoriesUseCase
import app.tinygiants.getalife.domain.usecase.transaction.AddTransactionUseCase
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.time.Clock
import kotlin.time.Instant

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val getCategories: GetCategoriesUseCase,
    private val getAccounts: GetAccountsUseCase,
    private val addTransaction: AddTransactionUseCase,
    private val categoryRepository: CategoryRepository,
    private val onboardingPrefsUseCase: OnboardingPrefsUseCase,
    private val addAccount: AddAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState = _uiState.asStateFlow()

    // region Init

    init {
        loadCategories()
        loadAccounts()
        initializeUiMode()
    }

    private fun initializeUiMode() {
        viewModelScope.launch {
            val isTransactionOnboardingCompleted = onboardingPrefsUseCase.isTransactionOnboardingCompletedFlow.first()
            _uiState.update {
                it.copy(
                    isGuidedMode = !isTransactionOnboardingCompleted,
                    currentStep = if (isTransactionOnboardingCompleted) TransactionStep.FlowSelection else TransactionStep.FlowSelection,
                    currentStepTitle = getStepTitle(TransactionStep.FlowSelection, TransactionInput())
                )
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getCategories()
                .catch { throwable -> Firebase.crashlytics.recordException(throwable) }
                .collect { categories ->
                    _uiState.update { uiState -> uiState.copy(categories = categories) }
                }
        }
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            getAccounts()
                .catch { throwable -> Firebase.crashlytics.recordException(throwable) }
                .collect { result ->
                    result.onSuccess { accounts ->
                        _uiState.update { uiState -> uiState.copy(accounts = accounts) }
                    }
                }
        }
    }

    // endregion

    // region Transaction Input Management

    /**
     * Updates the transaction input and handles flow-specific logic.
     * This is the central method for all input changes.
     */
    private fun updateTransactionInput(update: (TransactionInput) -> TransactionInput) {
        _uiState.update { currentState ->
            val newInput = update(currentState.transactionInput)
            currentState.copy(
                transactionInput = newInput,
                isFormValid = newInput.isValid(),
                currentStepTitle = getStepTitle(currentState.currentStep, newInput)
            )
        }
    }

    /**
     * Selects the transaction direction and cleans up irrelevant fields.
     */
    fun onTransactionDirectionSelected(direction: TransactionDirection) {
        updateTransactionInput { it.updateDirection(direction) }

        // Auto-advance to next step in guided mode
        if (uiState.value.isGuidedMode) {
            moveToNextStep()
        }
    }

    /**
     * Updates the transaction amount.
     */
    fun onAmountChanged(amount: Money) {
        updateTransactionInput { it.copy(amount = amount) }
    }

    /**
     * Selects the source account for the transaction.
     */
    fun onFromAccountSelected(account: Account) {
        updateTransactionInput { it.copy(fromAccount = account) }

        // Auto-advance to next step in guided mode
        if (uiState.value.isGuidedMode) {
            moveToNextStep()
        }
    }

    /**
     * Selects the destination account for transfers.
     */
    fun onToAccountSelected(account: Account) {
        updateTransactionInput { it.copy(toAccount = account) }

        // Auto-advance to next step in guided mode
        if (uiState.value.isGuidedMode) {
            moveToNextStep()
        }
    }

    /**
     * Updates the transaction partner.
     */
    fun onPartnerChanged(partner: String) {
        updateTransactionInput { it.copy(partner = partner) }
    }

    /**
     * Selects a category for outflow transactions.
     */
    fun onCategorySelected(category: Category?) {
        updateTransactionInput { it.copy(category = category) }

        // Auto-advance to next step in guided mode
        if (uiState.value.isGuidedMode && category != null) {
            moveToNextStep()
        }
    }

    /**
     * Updates the transaction date.
     */
    fun onDateSelected(date: LocalDate) {
        updateTransactionInput { it.copy(date = date) }
    }

    /**
     * Updates the transaction description.
     */
    fun onDescriptionChanged(description: String) {
        updateTransactionInput { it.copy(description = description) }
    }

    // endregion

    // region Step Navigation (Guided Mode)

    /**
     * Moves to the next step in the guided flow based on current input state.
     */
    fun moveToNextStep() {
        val nextStep = uiState.value.getNextStep()
        _uiState.update {
            it.copy(
                currentStep = nextStep,
                currentStepTitle = getStepTitle(nextStep, it.transactionInput)
            )
        }
    }

    /**
     * Navigates to a specific step (allows going back in guided mode).
     */
    fun goToStep(step: TransactionStep) {
        _uiState.update {
            it.copy(
                currentStep = step,
                currentStepTitle = getStepTitle(step, it.transactionInput)
            )
        }
    }

    /**
     * Returns the current step title for display based on transaction context.
     */
    private fun getStepTitle(step: TransactionStep, transactionInput: TransactionInput): String {
        return when (step) {
            TransactionStep.FlowSelection -> "Was möchten Sie tun?"
            TransactionStep.Amount -> when (transactionInput.direction) {
                TransactionDirection.Inflow -> "Wie viel haben Sie erhalten?"
                TransactionDirection.Outflow -> "Wie viel haben Sie ausgegeben?"
                TransactionDirection.AccountTransfer -> "Wie viel möchten Sie überweisen?"
                else -> "Betrag eingeben"
            }

            TransactionStep.FromAccount -> when (transactionInput.direction) {
                TransactionDirection.Inflow -> "Auf welches Konto?"
                TransactionDirection.Outflow -> "Von welchem Konto?"
                TransactionDirection.AccountTransfer -> "Von welchem Konto?"
                else -> "Konto auswählen"
            }

            TransactionStep.ToAccount -> "Auf welches Konto überweisen?"
            TransactionStep.Partner -> when (transactionInput.direction) {
                TransactionDirection.Inflow -> "Von wem haben Sie Geld erhalten?"
                TransactionDirection.Outflow -> "Wo haben Sie das Geld ausgegeben?"
                else -> "Partner eingeben"
            }

            TransactionStep.Category -> "Für welche Kategorie?"
            TransactionStep.Date -> "Wann war das?"
            TransactionStep.Optional -> "Möchten Sie eine Notiz hinzufügen?"
            TransactionStep.Done -> "Geschafft! 🎉"
        }
    }

    // endregion

    // region Account and Category Creation

    /**
     * Create and persist a new account, then reload the accounts list.
     */
    fun onAccountCreated(name: String, initialBalance: Money, type: AccountType) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loadingState = it.loadingState.copy(isCreatingAccount = true)) }

                addAccount(
                    name = name,
                    balance = initialBalance,
                    type = type,
                    startingBalanceName = "Startsaldo",
                    startingBalanceDescription = "Anfangsguthaben für $name"
                )

                _uiState.update { it.copy(loadingState = it.loadingState.copy(isCreatingAccount = false)) }
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
                _uiState.update {
                    it.copy(
                        loadingState = it.loadingState.copy(isCreatingAccount = false),
                        errorState = it.errorState.copy(error = UiError.GenericError("Fehler beim Erstellen des Kontos: ${e.message}"))
                    )
                }
            }
        }
    }

    /**
     * Create and persist a new category, then reload the categories list.
     */
    fun onCategoryCreated(name: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loadingState = it.loadingState.copy(isCreatingCategory = true)) }

                val newCategory = Category(
                    id = 0,
                    groupId = 1L,
                    emoji = "",
                    name = name,
                    budgetTarget = Money(0.0),
                    monthlyTargetAmount = null,
                    targetMonthsRemaining = null,
                    listPosition = uiState.value.categories.size,
                    isInitialCategory = false,
                    linkedAccountId = null,
                    updatedAt = Clock.System.now(),
                    createdAt = Clock.System.now()
                )

                categoryRepository.addCategory(newCategory)
                _uiState.update {
                    it.copy(
                        loadingState = it.loadingState.copy(isCreatingCategory = false),
                        errorState = it.errorState.copy(error = null)
                    )
                }
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
                _uiState.update {
                    it.copy(
                        loadingState = it.loadingState.copy(isCreatingCategory = false),
                        errorState = it.errorState.copy(error = UiError.GenericError("Fehler beim Erstellen der Kategorie: ${e.message}"))
                    )
                }
            }
        }
    }

    // endregion

    // region Transaction Saving

    /**
     * Saves the transaction using the current TransactionInput state.
     * This is the unified save method for both guided and standard modes.
     */
    fun saveTransaction(recurrenceFrequency: RecurrenceFrequency? = null) {
        val input = uiState.value.transactionInput

        if (!input.isValid()) {
            _uiState.update {
                it.copy(
                    errorState = it.errorState.copy(
                        error = UiError.ValidationError(
                            "form",
                            "Bitte füllen Sie alle erforderlichen Felder aus."
                        )
                    )
                )
            }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(loadingState = it.loadingState.copy(isSavingTransaction = true)) }

                // Handle category creation if needed
                val finalCategory = input.category?.let { category ->
                    if (category.id < 0) {
                        val newCategory = category.copy(id = 0)
                        categoryRepository.addCategory(newCategory)
                        newCategory
                    } else {
                        category
                    }
                }

                // Save transaction using the appropriate flow
                when (input.direction!!) {
                    TransactionDirection.Inflow -> saveInflowTransaction(input, finalCategory, recurrenceFrequency)
                    TransactionDirection.Outflow -> saveOutflowTransaction(input, finalCategory!!, recurrenceFrequency)
                    TransactionDirection.AccountTransfer -> saveTransferTransaction(input, recurrenceFrequency)
                    else -> throw IllegalStateException("Unsupported transaction direction: ${input.direction}")
                }

                // Mark onboarding as completed if in guided mode
                if (uiState.value.isGuidedMode) {
                    onboardingPrefsUseCase.markTransactionOnboardingCompleted()
                    _uiState.update { it.copy(currentStep = TransactionStep.Done) }
                } else {
                    // Reset form for new transaction in standard mode
                    resetTransactionInput()
                }

                _uiState.update {
                    it.copy(
                        loadingState = it.loadingState.copy(isSavingTransaction = false),
                        errorState = it.errorState.copy(error = null)
                    )
                }
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
                _uiState.update {
                    it.copy(
                        loadingState = it.loadingState.copy(isSavingTransaction = false),
                        errorState = it.errorState.copy(error = UiError.GenericError("Fehler beim Speichern der Transaktion: ${e.message}"))
                    )
                }
            }
        }
    }

    private suspend fun saveInflowTransaction(
        input: TransactionInput,
        category: Category?,
        recurrenceFrequency: RecurrenceFrequency?
    ) {
        addTransaction(
            accountId = input.fromAccount!!.id,
            category = category,
            amount = input.amount!!,
            direction = TransactionDirection.Inflow,
            transactionPartner = input.partner,
            description = input.description,
            dateOfTransaction = input.date?.let {
                Instant.fromEpochSeconds(it.toEpochDay() * 24 * 60 * 60)
            } ?: Clock.System.now(),
            recurrenceFrequency = recurrenceFrequency
        )
    }

    private suspend fun saveOutflowTransaction(
        input: TransactionInput,
        category: Category,
        recurrenceFrequency: RecurrenceFrequency?
    ) {
        addTransaction(
            accountId = input.fromAccount!!.id,
            category = category,
            amount = input.amount!!,
            direction = TransactionDirection.Outflow,
            transactionPartner = input.partner,
            description = input.description,
            dateOfTransaction = input.date?.let {
                Instant.fromEpochSeconds(it.toEpochDay() * 24 * 60 * 60)
            } ?: Clock.System.now(),
            recurrenceFrequency = recurrenceFrequency
        )
    }

    private suspend fun saveTransferTransaction(
        input: TransactionInput,
        recurrenceFrequency: RecurrenceFrequency?
    ) {
        addTransaction(
            accountId = input.fromAccount!!.id,
            category = null, // Transfers don't have categories
            amount = input.amount!!,
            direction = TransactionDirection.AccountTransfer,
            transactionPartner = "Transfer zu ${input.toAccount!!.name}",
            description = input.description.ifBlank { "Transfer zwischen Konten" },
            dateOfTransaction = input.date?.let {
                Instant.fromEpochSeconds(it.toEpochDay() * 24 * 60 * 60)
            } ?: Clock.System.now(),
            recurrenceFrequency = recurrenceFrequency
        )
    }

    // endregion

    // region Mode Switching and Reset

    /**
     * Switch from guided mode to standard mode.
     */
    fun switchToStandardMode() {
        _uiState.update {
            it.copy(
                isGuidedMode = false,
                currentStep = TransactionStep.FlowSelection,
                currentStepTitle = getStepTitle(TransactionStep.FlowSelection, it.transactionInput)
            )
        }
        resetTransactionInput()
    }

    /**
     * Switch from standard mode to guided mode.
     */
    fun switchToGuidedMode() {
        _uiState.update {
            it.copy(
                isGuidedMode = true,
                currentStep = TransactionStep.FlowSelection,
                currentStepTitle = getStepTitle(TransactionStep.FlowSelection, it.transactionInput)
            )
        }
        resetTransactionInput()
    }

    /**
     * Resets the transaction input to start a new transaction.
     */
    fun resetTransactionInput() {
        _uiState.update {
            it.copy(
                transactionInput = TransactionInput(),
                currentStep = TransactionStep.FlowSelection,
                isFormValid = false,
                errorState = ErrorState(),
                currentStepTitle = getStepTitle(TransactionStep.FlowSelection, TransactionInput())
            )
        }
    }

    // endregion
}