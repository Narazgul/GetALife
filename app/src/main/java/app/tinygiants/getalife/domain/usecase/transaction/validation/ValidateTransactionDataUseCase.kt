package app.tinygiants.getalife.domain.usecase.transaction.validation

import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.RecurrenceFrequency
import app.tinygiants.getalife.domain.model.TransactionDirection
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case responsible for validating transaction data before processing.
 * Ensures all transaction parameters are valid and consistent.
 */
class ValidateTransactionDataUseCase @Inject constructor(
    @Default private val defaultDispatcher: CoroutineDispatcher
) {

    data class TransactionValidationData(
        val accountId: Long,
        val amount: Money,
        val direction: TransactionDirection,
        val transactionPartner: String,
        val description: String,
        val recurrenceFrequency: RecurrenceFrequency?
    )

    /**
     * Validates transaction data and throws IllegalArgumentException if invalid.
     * Returns Unit if validation passes.
     */
    suspend operator fun invoke(data: TransactionValidationData): Unit = withContext(defaultDispatcher) {
        validateTransactionDirection(data.direction)
        validateTransactionAmount(data.amount)
        validateRecurrenceData(data.recurrenceFrequency)
        validateTransactionPartner(data.transactionPartner)
        validateAccountId(data.accountId)
    }

    private fun validateTransactionDirection(direction: TransactionDirection) {
        require(direction != TransactionDirection.Unknown) {
            "Transaction direction cannot be Unknown"
        }
    }

    private fun validateTransactionAmount(amount: Money) {
        require(amount.asDouble() > 0) {
            "Transaction amount must be positive, was: ${amount.formattedMoney}"
        }
    }

    private fun validateRecurrenceData(recurrenceFrequency: RecurrenceFrequency?) {
        if (recurrenceFrequency != null) {
            require(recurrenceFrequency != RecurrenceFrequency.NEVER) {
                "Recurring transactions cannot have frequency NEVER"
            }
        }
    }

    private fun validateTransactionPartner(transactionPartner: String) {
        require(transactionPartner.isNotBlank()) {
            "Transaction partner cannot be blank"
        }
        require(transactionPartner.length <= 100) {
            "Transaction partner name too long: ${transactionPartner.length} characters (max 100)"
        }
    }

    private fun validateAccountId(accountId: Long) {
        require(accountId > 0) {
            "Account ID must be positive, was: $accountId"
        }
    }
}