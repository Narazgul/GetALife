package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction

import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import java.time.LocalDate

data class AddTransactionUiState(
    val categories: List<Category>,
    val accounts: List<Account>,
    val selectedCategory: Category? = null,
    val isGuidedMode: Boolean = false,
    val guidedStep: GuidedTransactionStep = GuidedTransactionStep.Type,
    // Guided mode transaction data
    val selectedDirection: TransactionDirection? = null,
    val selectedAmount: Money? = null,
    val selectedAccount: Account? = null,
    val selectedToAccount: Account? = null, // For transfers
    val selectedPartner: String = "",
    val selectedDate: LocalDate? = null,
    val selectedDescription: String = ""
)