package app.tinygiants.getalife.presentation.main_app.transaction.transactions.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.model.asStringRes
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing
import app.tinygiants.getalife.theme.success
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@Composable
fun SingleTransactionItem(
    transaction: Transaction,
    accounts: List<Account>,
    categories: List<Category>,
    onSaveTransactionClicked: (transaction: Transaction) -> Unit = {},
    onDeleteTransactionClicked: () -> Unit = {}
) {
    var isUpdateTransactionBottomSheetVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spacing.m, vertical = spacing.s)
            .clickable { isUpdateTransactionBottomSheetVisible = true },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(spacing.l)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.l),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side with icon and transaction info
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.m)
            ) {
                // Transaction direction icon in circular background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (transaction.transactionDirection == TransactionDirection.Inflow)
                                success.copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (transaction.transactionDirection == TransactionDirection.Inflow)
                            Icons.Default.KeyboardArrowUp
                        else
                            Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = if (transaction.transactionDirection == TransactionDirection.Inflow)
                            success
                        else
                            MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Transaction details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Transaction partner (main text)
                    Text(
                        text = transaction.transactionPartner.ifEmpty { "Unknown" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(spacing.xs))

                    // Category
                    Text(
                        text = transaction.category?.name ?: "Nicht zugeordnet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(spacing.xs))

                    // Date
                    Text(
                        text = formatTransactionDate(transaction.dateOfTransaction),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Recurring payment indicator
                    if (transaction.isRecurringTemplate) {
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(spacing.xs)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = transaction.recurrenceFrequency?.let {
                                    stringResource(it.asStringRes())
                                } ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Next payment date for recurring templates
                        transaction.nextPaymentDate?.let { nextDate ->
                            Spacer(modifier = Modifier.height(spacing.xs))
                            Text(
                                text = "${stringResource(app.tinygiants.getalife.R.string.next_execution)}: ${
                                    formatTransactionDate(
                                        nextDate
                                    )
                                }",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }

                    // Generated from recurring indicator
                    if (transaction.isGeneratedFromRecurring) {
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(
                            text = stringResource(app.tinygiants.getalife.R.string.automatically_created),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }

                    // Description (if available)
                    if (transaction.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(spacing.xs))
                        Text(
                            text = transaction.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(spacing.m))

            // Right side with amount
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = transaction.amount.formattedPositiveMoney,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (transaction.transactionDirection == TransactionDirection.Inflow)
                        success
                    else
                        MaterialTheme.colorScheme.error
                )

                Text(
                    text = if (transaction.transactionDirection == TransactionDirection.Inflow) "Eingang" else "Ausgang",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (transaction.transactionDirection == TransactionDirection.Inflow)
                        success
                    else
                        MaterialTheme.colorScheme.error
                )
            }
        }
    }

    val onSaveClicked =
        { updatedAmount: Money,
          updatedAccount: Account,
          updatedCategory: Category?,
          updatedTransactionDirection: TransactionDirection,
          updatedDescription: String,
          updatedTransactionPartner: String,
          updatedDateOfTransaction: kotlin.time.Instant ->

            val updatedTransaction = transaction.copy(
                amount = updatedAmount,
                account = updatedAccount,
                category = updatedCategory,
                transactionDirection = updatedTransactionDirection,
                description = updatedDescription,
                transactionPartner = updatedTransactionPartner,
                dateOfTransaction = updatedDateOfTransaction
            )

            onSaveTransactionClicked(updatedTransaction)
        }

    if (isUpdateTransactionBottomSheetVisible) {
        TransactionBottomSheet(
            transaction = transaction,
            categories = categories,
            accounts = accounts,
            onDismissRequest = { isUpdateTransactionBottomSheetVisible = false },
            onSaveTransactionClicked = onSaveClicked,
            onDeleteTransactionClicked = onDeleteTransactionClicked,
        )
    }
}

private fun formatTransactionDate(instant: kotlin.time.Instant): String {
    val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${localDate.day.toString().padStart(2, '0')}.${
        localDate.month.number.toString().padStart(2, '0')
    }.${localDate.year}"
}

@Preview
@Composable
private fun TransactionItemPreview() {
    GetALifeTheme {
        Surface {
            SingleTransactionItem(
                transaction = Transaction(
                    id = 0,
                    amount = Money(value = -3.20),
                    account = Account(-1, "", Money(0.00), AccountType.Unknown, 0, false, Clock.System.now(), Clock.System.now()),
                    category = null,
                    transactionPartner = "Telekom",
                    transactionDirection = TransactionDirection.Outflow,
                    description = "Internet Bill",
                    dateOfTransaction = Clock.System.now(),
                    updatedAt = Clock.System.now(),
                    createdAt = Clock.System.now()
                ), accounts = emptyList(), categories = emptyList()
            )
        }
    }
}