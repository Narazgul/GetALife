package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction

import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import java.time.LocalDate

/**
 * Central data model for all transaction input flows.
 * Used by both Guided and Standard modes to ensure consistency.
 *
 * Contains all possible fields needed across all transaction types:
 * - Inflow: direction, amount, fromAccount, partner, date, description
 * - Outflow: direction, amount, fromAccount, partner, category, date, description
 * - Transfer: direction, amount, fromAccount, toAccount, date, description
 *
 * Note: TransactionDirection.Unknown is the initial state before user selection.
 * Once a direction is selected, user can change between Inflow/Outflow/AccountTransfer
 * but cannot return to Unknown state.
 */
data class TransactionInput(
    val direction: TransactionDirection? = null, // null = no selection made yet (equivalent to Unknown)
    val amount: Money? = null,
    val fromAccount: Account? = null,
    val toAccount: Account? = null, // Only used for AccountTransfer
    val partner: String = "",
    val category: Category? = null, // Only used for Outflow
    val date: LocalDate? = null,
    val description: String = ""
) {

    /**
     * Returns true if user has made an initial direction selection.
     * Once true, user can change between valid directions but not return to Unknown.
     */
    fun hasDirectionSelected(): Boolean = direction != null && direction != TransactionDirection.Unknown

    /**
     * Validates if the current input is sufficient for an Inflow transaction.
     * Required: direction, amount, fromAccount
     * Optional: partner, date, description
     */
    fun isValidForInflow(): Boolean =
        direction == TransactionDirection.Inflow &&
                amount != null &&
                fromAccount != null

    /**
     * Validates if the current input is sufficient for an Outflow transaction.
     * Required: direction, amount, fromAccount, category
     * Optional: partner, date, description
     */
    fun isValidForOutflow(): Boolean =
        direction == TransactionDirection.Outflow &&
                amount != null &&
                fromAccount != null &&
                category != null

    /**
     * Validates if the current input is sufficient for a Transfer transaction.
     * Required: direction, amount, fromAccount, toAccount
     * Optional: date, description
     * Note: partner and category are not used for transfers
     */
    fun isValidForTransfer(): Boolean =
        direction == TransactionDirection.AccountTransfer &&
                amount != null &&
                fromAccount != null &&
                toAccount != null &&
                fromAccount != toAccount // Prevent transfer to same account

    /**
     * Validates the current input based on the selected transaction direction.
     * Returns true if all required fields for the current flow are filled.
     */
    fun isValidForCurrentFlow(): Boolean = when (direction) {
        TransactionDirection.Inflow -> isValidForInflow()
        TransactionDirection.Outflow -> isValidForOutflow()
        TransactionDirection.AccountTransfer -> isValidForTransfer()
        TransactionDirection.Unknown, null -> false // Cannot be valid in unknown state
        else -> false // CreditCardPayment or other unsupported directions
    }

    /**
     * Returns true if this is the initial state (no direction selected yet).
     * Used to determine if we should show the flow selection screen.
     */
    fun isInitialState(): Boolean = direction == null || direction == TransactionDirection.Unknown

    /**
     * Creates a copy with updated direction and resets fields that are not relevant for the new flow.
     * This ensures clean state when user switches between flows.
     * Note: Once a direction is selected, user cannot return to Unknown state.
     */
    fun updateDirection(newDirection: TransactionDirection): TransactionInput {
        // Prevent returning to Unknown state once a selection has been made
        if (newDirection == TransactionDirection.Unknown && hasDirectionSelected()) {
            return this
        }

        return when (newDirection) {
            TransactionDirection.Inflow -> copy(
                direction = newDirection,
                toAccount = null, // Not used for inflows
                category = null // Not used for inflows
            )

            TransactionDirection.Outflow -> copy(
                direction = newDirection,
                toAccount = null // Not used for outflows
            )

            TransactionDirection.AccountTransfer -> copy(
                direction = newDirection,
                partner = "", // Not used for transfers
                category = null // Not used for transfers
            )

            else -> copy(direction = newDirection)
        }
    }
}