package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction

import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import java.time.LocalDate

/**
 * Enhanced UI State for Add Transaction screen with comprehensive error handling and loading states.
 */
data class AddTransactionUiState(
    // Core data
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),

    // Loading states - improve UX during async operations
    val isLoading: Boolean = false,
    val isCreatingAccount: Boolean = false,
    val isCreatingCategory: Boolean = false,
    val isSavingTransaction: Boolean = false,

    // Error handling - provide clear feedback to users
    val error: UiError? = null,
    val fieldErrors: Map<String, String> = emptyMap(),

    // Guided mode state
    val isGuidedMode: Boolean = true,
    val guidedStep: GuidedTransactionStep = GuidedTransactionStep.Type,

    // Transaction form data
    val selectedDirection: TransactionDirection? = null,
    val selectedAmount: Money? = null,
    val selectedAccount: Account? = null,
    val selectedToAccount: Account? = null, // For transfers
    val selectedPartner: String = "",
    val selectedCategory: Category? = null,
    val selectedDate: LocalDate? = null,
    val selectedDescription: String = "",

    // Validation state - track which fields are valid
    val isFormValid: Boolean = true,
    val validationErrors: Set<ValidationError> = emptySet()
) {
    /**
     * Check if current step in guided mode can proceed
     */
    val canProceedFromCurrentStep: Boolean
        get() = when (guidedStep) {
            GuidedTransactionStep.Type -> selectedDirection != null
            GuidedTransactionStep.Amount -> selectedAmount?.asDouble() ?: 0.0 > 0
            GuidedTransactionStep.Account -> selectedAccount != null
            GuidedTransactionStep.ToAccount -> selectedToAccount != null || selectedDirection != TransactionDirection.Unknown
            GuidedTransactionStep.Partner -> selectedPartner.isNotBlank()
            GuidedTransactionStep.Category -> selectedCategory != null || selectedDirection == TransactionDirection.Unknown
            GuidedTransactionStep.Date -> selectedDate != null
            GuidedTransactionStep.Optional -> true // Optional step
            GuidedTransactionStep.Done -> true
        }

    /**
     * Check if we have all required data to save transaction
     */
    val canSaveTransaction: Boolean
        get() = selectedDirection != null &&
                selectedAmount != null &&
                selectedAmount!!.asDouble() > 0 &&
                selectedAccount != null &&
                selectedPartner.isNotBlank() &&
                validationErrors.isEmpty() &&
                // For transfers, we need a destination account
                (selectedDirection != TransactionDirection.Unknown || selectedToAccount != null)
}

/**
 * Comprehensive error types for better user feedback
 */
sealed class UiError(
    val message: String,
    val isRetryable: Boolean = false
) {
    data class NetworkError(val details: String) : UiError("Netzwerkfehler: $details", isRetryable = true)
    data class ValidationError(val field: String, val details: String) : UiError("Eingabefehler in $field: $details")
    data class BusinessLogicError(val details: String) : UiError("Fehler: $details")
    data class UnknownError(val details: String) : UiError("Unbekannter Fehler: $details", isRetryable = true)

    // Smart categorization specific errors
    data class CategorizationError(val details: String) :
        UiError("KI-Kategorisierung fehlgeschlagen: $details", isRetryable = true)

    // Account/Category creation errors  
    data class AccountCreationError(val details: String) :
        UiError("Konto konnte nicht erstellt werden: $details", isRetryable = true)

    data class CategoryCreationError(val details: String) :
        UiError("Kategorie konnte nicht erstellt werden: $details", isRetryable = true)
}

/**
 * Specific validation errors for form fields
 */
enum class ValidationError(val message: String) {
    AMOUNT_ZERO_OR_NEGATIVE("Betrag muss größer als 0 sein"),
    AMOUNT_TOO_LARGE("Betrag ist zu groß"),
    PARTNER_EMPTY("Transaktionspartner ist erforderlich"),
    PARTNER_TOO_LONG("Transaktionspartner ist zu lang"),
    ACCOUNT_NOT_SELECTED("Bitte wähle ein Konto aus"),
    TO_ACCOUNT_NOT_SELECTED("Bitte wähle ein Zielkonto aus"),
    TO_ACCOUNT_SAME_AS_FROM("Quell- und Zielkonto können nicht identisch sein"),
    DESCRIPTION_TOO_LONG("Beschreibung ist zu lang"),
    DATE_IN_FUTURE("Datum kann nicht in der Zukunft liegen"),
    INSUFFICIENT_FUNDS("Nicht genügend Guthaben auf dem Konto") // For future account balance validation
}