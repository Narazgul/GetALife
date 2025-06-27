package app.tinygiants.getalife.presentation.main_app.account.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.success
import app.tinygiants.getalife.theme.warning

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountItem(
    name: String = "",
    balance: Money = Money(value = 0.00),
    type: AccountType = AccountType.Unknown,
    onNavigateToAccountDetails: () -> Unit = { },
    onUpdateAccountClicked: (accountName: String, type: AccountType) -> Unit = { _, _ -> },
    onDeleteAccountClicked: () -> Unit = { }
) {
    var isAccountDialogVisible by rememberSaveable { mutableStateOf(false) }

    val moneyColor = when {
        balance > EmptyMoney() -> success
        balance == EmptyMoney() -> MaterialTheme.colorScheme.outline
        else -> warning
    }

    val backgroundColor = MaterialTheme.colorScheme.surface
    val gradient = Brush.horizontalGradient(
        colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        tonalElevation = 4.dp,
        shadowElevation = 6.dp,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .combinedClickable(
                onClick = { onNavigateToAccountDetails() },
                onLongClick = { isAccountDialogVisible = true }
            )
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circle with gradient
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(gradient, shape = CircleShape)
            ) {
                Icon(
                    imageVector = when (type) {
                        AccountType.CreditCard -> Icons.Default.AccountBox
                        AccountType.Depot -> Icons.Default.AccountCircle
                        AccountType.Checking -> Icons.Default.Star
                        else -> Icons.Default.AccountBox
                    },
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(28.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = type.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = balance.formattedMoney,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = moneyColor
            )
        }
    }

    if (isAccountDialogVisible) EditAccountBottomSheet(
        accountName = name,
        type = type,
        onConfirmClicked = onUpdateAccountClicked,
        onDeleteAccountClicked = onDeleteAccountClicked,
        onDismissRequest = { isAccountDialogVisible = false }
    )
}

@PreviewLightDark
@Composable
fun AccountItemPreview(@PreviewParameter(AccountItemPreviewProvider::class) money: Money) {
    GetALifeTheme {
        Surface {
            AccountItem(
                name = "Tagesgeldkonto",
                balance = money,
                type = AccountType.Savings
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