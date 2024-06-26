package app.tinygiants.getalife.presentation.transaction.add_transaction

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.presentation.transaction.composables.TransactionBottomSheet

@Composable
fun AddTransaction() {
    val viewModel: AddTransactionViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val onAddTransactionClicked =
        { amount: Money, accountId: Long, direction: TransactionDirection, description: String?,
          transactionPartner: String?, category: Category ->
            viewModel.onSaveTransactionClicked(
                amount = amount,
                accountId = accountId,
                direction = direction,
                description = description,
                transactionPartner = transactionPartner,
                category = category
            )
        }

    TransactionBottomSheet(
        transaction = null,
        categories = uiState.categories,
        accounts = uiState.accounts,
        onConfirmClicked = onAddTransactionClicked
    )
}