package app.tinygiants.getalife.presentation.transaction.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.presentation.transaction.transactions.UserClickEvent
import app.tinygiants.getalife.theme.GetALifeTheme

@Composable
fun TransactionsList(
    modifier: Modifier = Modifier,
    transactions: List<Transaction> = emptyList(),
    accounts: List<Account> = emptyList(),
    categories: List<Category> = emptyList(),
    onUserClickEvent: (UserClickEvent) -> Unit = {}
) {
    Surface(
        modifier = modifier.background(Color.White),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(items = transactions, key = { transaction -> transaction.id }) { transaction ->

                    val onUpdateTransactionClicked = { updatedTransaction: Transaction ->
                        onUserClickEvent(UserClickEvent.UpdateTransaction(transaction = updatedTransaction))
                    }
                    val onExchangeCategoryClicked = { updatedTransaction: Transaction, oldCategory: Category? ->
                        onUserClickEvent(UserClickEvent.ExchangeCategory(transaction = updatedTransaction, oldCategory = oldCategory))
                    }
                    val onDeleteTransactionClicked = {
                        onUserClickEvent(UserClickEvent.DeleteTransaction(transaction = transaction))
                    }

                    TransactionItem(
                        transaction = transaction,
                        accounts = accounts,
                        categories = categories,
                        onUpdateTransactionClicked = onUpdateTransactionClicked,
                        onExchangeCategoryClicked = onExchangeCategoryClicked,
                        onDeleteTransactionClicked = onDeleteTransactionClicked
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun TransactionsListDialogPreview() {
    GetALifeTheme {
        Surface {
            TransactionsList()
        }
    }
}