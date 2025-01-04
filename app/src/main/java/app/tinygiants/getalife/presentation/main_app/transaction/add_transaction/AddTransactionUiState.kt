package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction

import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category

data class AddTransactionUiState(
    val categories: List<Category>,
    val accounts: List<Account>,
)