package app.tinygiants.getalife.presentation.main_app.transaction.transactions.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.presentation.main_app.transaction.transactions.UserClickEvent
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing

@Composable
fun TransactionsList(
    modifier: Modifier = Modifier,
    transactions: List<Transaction> = emptyList(),
    accounts: List<Account> = emptyList(),
    categories: List<Category> = emptyList(),
    onUserClickEvent: (UserClickEvent) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = spacing.s)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = spacing.xl),
            verticalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            items(items = transactions, key = { transaction -> transaction.id }) { transaction ->

                val onSaveTransactionClicked = { updatedTransaction: Transaction ->
                    onUserClickEvent(UserClickEvent.SaveTransaction(transaction = updatedTransaction))
                }
                val onDeleteTransactionClicked = {
                    onUserClickEvent(UserClickEvent.DeleteTransaction(transaction = transaction))
                }

                SingleTransactionItem(
                    transaction = transaction,
                    accounts = accounts,
                    categories = categories,
                    onSaveTransactionClicked = onSaveTransactionClicked,
                    onDeleteTransactionClicked = onDeleteTransactionClicked
                )
            }
        }
    }
}

@Preview
@Composable
private fun TransactionsListDialogPreview() {
    GetALifeTheme {
        TransactionsList()
    }
}