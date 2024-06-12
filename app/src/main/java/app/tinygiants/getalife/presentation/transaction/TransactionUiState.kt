package app.tinygiants.getalife.presentation.transaction

import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.presentation.composables.ErrorMessage

data class TransactionUiState(
    val title: String,
    val transactions: List<Transaction>,
    val isLoading: Boolean,
    val errorMessage: ErrorMessage?
)