package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.theme.GetALifeTheme
import kotlin.time.Clock

/**
 * Reusable transaction type selection component.
 * Displays segmented buttons for Inflow/Outflow/Transfer based on available accounts.
 */
@Composable
fun TransactionTypeSelector(
    selectedDirection: TransactionDirection?,
    availableAccounts: List<Account>,
    onTypeSelected: (TransactionDirection) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "Was für eine Transaktion ist das?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        // Build options based on available accounts
        val options = buildList {
            add(TransactionDirection.Inflow to "💰 Einnahme")
            // Only show Transfer if there are at least 2 accounts
            if (availableAccounts.size >= 2) {
                add(TransactionDirection.Unknown to "↔️ Transfer")
            }
            add(TransactionDirection.Outflow to "💸 Ausgabe")
        }

        MultiChoiceSegmentedButtonRow {
            options.forEachIndexed { idx, (direction, label) ->
                SegmentedButton(
                    checked = direction == selectedDirection,
                    onCheckedChange = {
                        // Always call onTypeSelected to allow re-selection
                        onTypeSelected(direction)
                    },
                    shape = when {
                        options.size == 1 -> RoundedCornerShape(16.dp)
                        idx == 0 -> RoundedCornerShape(
                            topStart = 16.dp,
                            bottomStart = 16.dp,
                            topEnd = 0.dp,
                            bottomEnd = 0.dp
                        )

                        idx == options.lastIndex -> RoundedCornerShape(
                            topEnd = 16.dp,
                            bottomEnd = 16.dp,
                            topStart = 0.dp,
                            bottomStart = 0.dp
                        )

                        else -> RoundedCornerShape(0.dp)
                    }
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

// Preview
@Preview(name = "Transaction Type Selector - All Options", showBackground = true)
@Composable
private fun TransactionTypeSelectorPreview() {
    GetALifeTheme {
        TransactionTypeSelector(
            selectedDirection = TransactionDirection.Outflow,
            availableAccounts = listOf(
                Account(
                    id = 1,
                    name = "Account 1",
                    balance = Money(100.0),
                    type = AccountType.Checking,
                    listPosition = 0,
                    updatedAt = Clock.System.now(),
                    createdAt = Clock.System.now()
                ),
                Account(
                    id = 2,
                    name = "Account 2",
                    balance = Money(200.0),
                    type = AccountType.Savings,
                    listPosition = 1,
                    updatedAt = Clock.System.now(),
                    createdAt = Clock.System.now()
                )
            ),
            onTypeSelected = { }
        )
    }
}

@Preview(name = "Transaction Type Selector - No Transfer Option", showBackground = true)
@Composable
private fun TransactionTypeSelectorNoTransferPreview() {
    GetALifeTheme {
        TransactionTypeSelector(
            selectedDirection = TransactionDirection.Inflow,
            availableAccounts = listOf(
                Account(
                    id = 1,
                    name = "Single Account",
                    balance = Money(50.0),
                    type = AccountType.Checking,
                    listPosition = 0,
                    updatedAt = Clock.System.now(),
                    createdAt = Clock.System.now()
                )
            ),
            onTypeSelected = { }
        )
    }
}