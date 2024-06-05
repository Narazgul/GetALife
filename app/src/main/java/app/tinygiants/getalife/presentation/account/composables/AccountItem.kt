package app.tinygiants.getalife.presentation.account.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.presentation.transaction.composables.TransactionDialog
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.md_theme_dark_outline
import app.tinygiants.getalife.theme.spacing
import app.tinygiants.getalife.theme.success
import app.tinygiants.getalife.theme.warning

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountItem(
    name: String = "",
    balance: Money = Money(value = 0.00),
    type: AccountType = AccountType.Unknown,
    categories: List<Category> = emptyList(),
    onUpdateAccountClicked: (accountName: String, balance: Money, type: AccountType) -> Unit = { _, _, _ -> },
    onDeleteAccountClicked: () -> Unit = { },
    onTransaction: (amount: Money?, direction: TransactionDirection?, description: String?, transactionPartner: String?, category: Category?) -> Unit
) {
    var isAccountDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isTransactionDialogVisible by rememberSaveable { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { isAccountDialogVisible = true },
                onLongClick = { isTransactionDialogVisible = true }
            )
            .padding(spacing.default),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.weight(1f)
        )
        val moneyColor = when {
            balance.value > 0.00 -> success
            balance.value == 0.00 -> md_theme_dark_outline
            else -> warning
        }
        Text(
            text = balance.formattedMoney,
            style = MaterialTheme.typography.titleMedium,
            color = moneyColor
        )
    }

    if (isAccountDialogVisible) AccountDialog(
        accountName = name,
        balance = balance,
        type = type,
        onConfirmClicked = onUpdateAccountClicked,
        onDeleteAccountClicked = onDeleteAccountClicked,
        onDismissRequest = { isAccountDialogVisible = false }
    )

    if (isTransactionDialogVisible) TransactionDialog(
        transaction = null,
        categories = categories,
        onConfirmClicked = onTransaction,
        onDismissRequest = { isTransactionDialogVisible = false }
    )
}

@PreviewLightDark
@Composable
fun AccountItemPreview(@PreviewParameter(AccountItemPreviewProvider::class) money: Money) {
    GetALifeTheme {
        Surface {
            AccountItem(
                name = "Girokonto",
                balance = money,
                onTransaction = { _, _, _, _, _ -> }
            )
        }
    }
}

class AccountItemPreviewProvider : PreviewParameterProvider<Money> {
    override val values: Sequence<Money>
        get() = sequenceOf(
            Money(value = 0.00),
            Money(value = -100.00),
            Money(value = 100.00)
        )
}