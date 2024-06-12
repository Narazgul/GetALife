package app.tinygiants.getalife.presentation.transaction.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.theme.GetALifeTheme

@Composable
fun TransactionsList(
    title: String,
    transactions: List<Transaction> = emptyList(),
    onTransactionClicked: () -> Unit = { }
) {
    Surface(
        modifier = Modifier.background(Color.White),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(items =  transactions, key = { transaction -> transaction.id }) { transaction ->
                    TransactionItem(
                        description = transaction.description,
                        category = transaction.category?.name ?: "Ready to assign",
                        amount = transaction.amount,
                        direction = transaction.direction
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
            TransactionsList(
                title = "Test"
            )
        }
    }
}