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
    val isFormValid: Boolean = true
)

/**
 * Comprehensive error types for better user feedback
 */
sealed class UiError(
    val message: String
)