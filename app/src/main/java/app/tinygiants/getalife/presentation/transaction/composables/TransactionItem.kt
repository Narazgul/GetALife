package app.tinygiants.getalife.presentation.transaction.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing
import app.tinygiants.getalife.theme.success
import kotlinx.datetime.Clock

@Composable
fun TransactionItem(
    transaction: Transaction,
    accounts: List<Account>,
    categories: List<Category>,
    onUpdateTransactionClicked: (transaction: Transaction) -> Unit = {},
    onDeleteTransactionClicked: () -> Unit = {}

) {
    var isUpdateTransactionBottomSheetVisible by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .padding(spacing.default)
            .clickable { isUpdateTransactionBottomSheetVisible = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(spacing.s))
            Text(
                text = transaction.category?.name ?: "Ready to assign",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(
            text = transaction.amount.formattedMoney,
            style = MaterialTheme.typography.bodyLarge,
            color = if (transaction.direction == TransactionDirection.Inflow) success else MaterialTheme.colorScheme.error
        )
    }

    if (isUpdateTransactionBottomSheetVisible)
        EditTransaction(
            transaction = transaction,
            accounts = accounts,
            categories = categories,
            onEditTransactionClicked = onUpdateTransactionClicked,
            onDeleteTransactionClicked = onDeleteTransactionClicked,
            onDismissRequest = { isUpdateTransactionBottomSheetVisible = false }
        )
}

@Preview
@Composable
private fun TransactionItemPreview() {
    GetALifeTheme {
        Surface {
            TransactionItem(
                transaction = Transaction(
                    id = 0,
                    amount = Money(value = -3.20),
                    account = Account(-1, "", Money(0.00), AccountType.Unknown, 0, Clock.System.now(), Clock.System.now()),
                    category = null,
                    transactionPartner = "Wimmer",
                    direction = TransactionDirection.Outflow,
                    description = "Bäckerei",
                    timestamp = Clock.System.now()
                ),
                accounts = emptyList(),
                categories = emptyList()
            )
        }
    }
}