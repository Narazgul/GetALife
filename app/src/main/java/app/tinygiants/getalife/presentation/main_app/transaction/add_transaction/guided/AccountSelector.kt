package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.theme.GetALifeTheme
import kotlin.time.Clock

/**
 * Specialized account selection component for guided transaction flows.
 * Handles empty state, account creation, and displays account balances.
 *
 * Performance optimized with stable callbacks to prevent unnecessary recompositions.
 */
@Composable
fun AccountSelector(
    title: String,
    accounts: List<Account>,
    selectedAccount: Account?,
    onAccountSelected: (Account) -> Unit,
    onCreateAccountClicked: () -> Unit,
    modifier: Modifier = Modifier,
    showBalances: Boolean = true
) {
    // Stable callback for create account - prevents recomposition of EmptyAccountState
    val stableCreateAccountCallback = remember { onCreateAccountClicked }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        if (accounts.isEmpty()) {
            // Empty state - show prominent "create account" option
            EmptyAccountState(
                onCreateClicked = stableCreateAccountCallback
            )
        } else {
            // Accounts available - show list with "add new" at bottom
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = accounts,
                    key = { account -> account.id } // Stable key for performance
                ) { account ->
                    // Create stable callback per account to prevent unnecessary recompositions
                    val stableAccountCallback = remember(account.id, onAccountSelected) {
                        { onAccountSelected(account) }
                    }

                    AccountCard(
                        account = account,
                        isSelected = account == selectedAccount,
                        showBalance = showBalances,
                        onClick = stableAccountCallback
                    )
                }

                // Add new account option at the bottom
                item(key = "add_new_account") { // Stable key
                    AddNewAccountCard(
                        onClick = stableCreateAccountCallback
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAccountState(
    onCreateClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCreateClicked() }
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(20.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🏦 Neues Konto erstellen",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Es sind noch keine Konten vorhanden",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AccountCard(
    account: Account,
    isSelected: Boolean,
    showBalance: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                )
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (showBalance) {
                        Text(
                            text = getAccountTypeEmoji(account.type),
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (showBalance) {
                    Text(
                        text = account.balance.formattedMoney,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (account.balance.asDouble() >= 0)
                            MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun AddNewAccountCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🏦",
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "Neues Konto erstellen",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Get emoji representation for account type
 */
private fun getAccountTypeEmoji(type: AccountType): String {
    return when (type) {
        AccountType.Cash -> "💵 Bargeld"
        AccountType.Checking -> "🏦 Girokonto"
        AccountType.Savings -> "🐷 Sparkonto"
        AccountType.CreditCard -> "💳 Kreditkarte"
        AccountType.Depot -> "📈 Depot"
        AccountType.Mortgage -> "🏠 Hypothek"
        AccountType.Loan -> "💰 Darlehen"
        else -> "🏦 Konto"
    }
}

// Preview data
private val previewAccounts = listOf(
    Account(
        id = 1L,
        name = "Hauptkonto",
        balance = Money(1250.75),
        type = AccountType.Checking,
        listPosition = 0,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    ),
    Account(
        id = 2L,
        name = "Sparkonto",
        balance = Money(5000.0),
        type = AccountType.Savings,
        listPosition = 1,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    ),
    Account(
        id = 3L,
        name = "Kreditkarte",
        balance = Money(-245.30),
        type = AccountType.CreditCard,
        listPosition = 2,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    )
)

// Previews
@Preview(name = "Account Selector - With Accounts", showBackground = true)
@Composable
private fun AccountSelectorPreview() {
    GetALifeTheme {
        AccountSelector(
            title = "Von welchem Konto soll das Geld abgehen?",
            accounts = previewAccounts,
            selectedAccount = previewAccounts[0],
            onAccountSelected = { },
            onCreateAccountClicked = { }
        )
    }
}

@Preview(name = "Account Selector - Empty State", showBackground = true)
@Composable
private fun AccountSelectorEmptyPreview() {
    GetALifeTheme {
        AccountSelector(
            title = "Von welchem Konto?",
            accounts = emptyList(),
            selectedAccount = null,
            onAccountSelected = { },
            onCreateAccountClicked = { }
        )
    }
}

@Preview(name = "Account Selector - No Balances", showBackground = true)
@Composable
private fun AccountSelectorNoBalancesPreview() {
    GetALifeTheme {
        AccountSelector(
            title = "Auf welches Konto soll das Geld?",
            accounts = previewAccounts,
            selectedAccount = previewAccounts[1],
            onAccountSelected = { },
            onCreateAccountClicked = { },
            showBalances = false
        )
    }
}