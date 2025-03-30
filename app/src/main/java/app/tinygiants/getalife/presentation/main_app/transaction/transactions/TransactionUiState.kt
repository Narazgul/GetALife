package app.tinygiants.getalife.presentation.main_app.transaction.transactions

import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.presentation.shared_composables.ErrorMessage

data class TransactionUiState(
    val title: String,
    val transactions: List<Transaction>,
    val accounts: List<Account>,
    val categories: List<Category>,
    val isLoading: Boolean,
    val errorMessage: ErrorMessage?
)

sealed class UserClickEvent {
    data class SaveTransaction(val transaction: Transaction) : UserClickEvent()
    data class DeleteTransaction(val transaction: Transaction) : UserClickEvent()
}