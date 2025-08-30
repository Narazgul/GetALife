package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction

import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import java.time.LocalDate


data class AddTransactionUiState(
    val categories: List<Category> = emptyList(),
    val accounts: List<Account> = emptyList(),
    val transactionInput: TransactionInput = TransactionInput(),
    val loadingState: LoadingState = LoadingState(),
    val errorState: ErrorState = ErrorState(),
    val isGuidedMode: Boolean = true,
    val currentStep: TransactionStep = TransactionStep.FlowSelection,
    val isFormValid: Boolean = transactionInput.isValid()
) {
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

enum class TransactionStep {
    FlowSelection,
    Amount,
    FromAccount,
    ToAccount,
    Partner,
    Category,
    Date,
    Optional,
    Done
}

data class LoadingState(
    val isLoading: Boolean = false,
    val isCreatingAccount: Boolean = false,
    val isCreatingCategory: Boolean = false,
    val isSavingTransaction: Boolean = false
)

data class ErrorState(
    val error: UiError? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)

sealed class UiError(
    val message: String
) {
    data class ValidationError(val field: String, val errorMessage: String) : UiError(errorMessage)
    data class GenericError(val errorMessage: String) : UiError(errorMessage)
}


data class TransactionInput(
    val direction: TransactionDirection = TransactionDirection.Unknown,
    val amount: Money? = null,
    val fromAccount: Account? = null,
    val toAccount: Account? = null,
    val partner: String = "",
    val category: Category? = null,
    val date: LocalDate? = null,
    val description: String = ""
) {

    fun hasDirectionSelected(): Boolean = direction != TransactionDirection.Unknown

    fun isValid(): Boolean = when (direction) {
        TransactionDirection.Inflow -> isValidInflow()
        TransactionDirection.Outflow -> isValidOutflow()
        TransactionDirection.AccountTransfer -> isValidTransfer()
        TransactionDirection.Unknown -> false
        else -> false
    }
    fun isInitialState(): Boolean = direction == TransactionDirection.Unknown

    private fun isValidInflow(): Boolean = amount != null && fromAccount != null
    private fun isValidOutflow(): Boolean = amount != null && fromAccount != null && category != null
    private fun isValidTransfer(): Boolean = amount != null && fromAccount != null && toAccount != null && fromAccount != toAccount

    fun updateDirection(newDirection: TransactionDirection): TransactionInput {
        if (newDirection == TransactionDirection.Unknown && hasDirectionSelected()) return this

        return when (newDirection) {
            TransactionDirection.Inflow -> copy(
                direction = newDirection,
                toAccount = null,
                category = null
            )

            TransactionDirection.Outflow -> copy(
                direction = newDirection,
                toAccount = null
            )

            TransactionDirection.AccountTransfer -> copy(
                direction = newDirection,
                partner = "",
                category = null
            )

            else -> copy(direction = newDirection)
        }
    }
}