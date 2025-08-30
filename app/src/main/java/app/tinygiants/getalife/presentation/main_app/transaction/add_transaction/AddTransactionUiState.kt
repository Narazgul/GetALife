package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction

import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.TransactionDirection

/**
 * Enhanced UI State for Add Transaction screen with centralized input model.
 *
 * Major Changes:
 * - Replaced individual transaction fields with centralized TransactionInput
 * - Maintained comprehensive error handling and loading states
 * - Simplified validation using TransactionInput logic
 */
data class AddTransactionUiState(
    // Core data
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),

    // Centralized transaction input - replaces all individual transaction fields
    val transactionInput: TransactionInput = TransactionInput(),

    // Loading states - improve UX during async operations
    val isLoading: Boolean = false,
    val isCreatingAccount: Boolean = false,
    val isCreatingCategory: Boolean = false,
    val isSavingTransaction: Boolean = false,

    // Error handling - provide clear feedback to users
    val error: UiError? = null,
    val fieldErrors: Map<String, String> = emptyMap(),

    // Mode and flow state
    val isGuidedMode: Boolean = true,
    val currentStep: TransactionStep = TransactionStep.FlowSelection,

    // Form validation state - now derived from TransactionInput
    val isFormValid: Boolean = transactionInput.isValidForCurrentFlow()
) {
    /**
     * Returns the next step in the transaction flow based on current input and selected direction.
     * Used for guided mode progression.
     */
    fun getNextStep(): TransactionStep = when {
        transactionInput.isInitialState() -> TransactionStep.FlowSelection
        transactionInput.amount == null -> TransactionStep.Amount
        transactionInput.fromAccount == null -> TransactionStep.FromAccount
        transactionInput.direction == TransactionDirection.AccountTransfer && transactionInput.toAccount == null -> TransactionStep.ToAccount
        transactionInput.direction != TransactionDirection.AccountTransfer && transactionInput.partner.isBlank() -> TransactionStep.Partner
        transactionInput.direction == TransactionDirection.Outflow && transactionInput.category == null -> TransactionStep.Category
        transactionInput.date == null -> TransactionStep.Date
        else -> TransactionStep.Optional
    }
}

/**
 * Unified step enum for both guided and standard modes.
 * Replaces the old GuidedTransactionStep enum with a more flexible approach.
 */
enum class TransactionStep {
    FlowSelection,  // Select Inflow, Outflow, or Transfer
    Amount,         // Enter transaction amount
    FromAccount,    // Select source account
    ToAccount,      // Select destination account (transfers only)
    Partner,        // Enter transaction partner (inflow/outflow only)
    Category,       // Select category (outflow only) 
    Date,           // Select transaction date
    Optional,       // Optional details (description, recurrence)
    Done            // Transaction completed
}

/**
 * Comprehensive error types for better user feedback
 */
sealed class UiError(
    val message: String
) {
    data class ValidationError(val field: String, val errorMessage: String) : UiError(errorMessage)
    data class GenericError(val errorMessage: String) : UiError(errorMessage)
}