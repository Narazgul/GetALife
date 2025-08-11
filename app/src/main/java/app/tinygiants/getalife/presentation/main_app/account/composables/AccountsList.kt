package app.tinygiants.getalife.presentation.main_app.account.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.presentation.main_app.account.UserClickEvent
import app.tinygiants.getalife.presentation.shared_composables.isScrollingDown
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing
import kotlin.time.Clock

@Composable
fun AccountsList(
    accounts: List<Account>,
    onNavigateToTransactionScreen: (accountId: Long) -> Unit,
    onUserScrolling: (Boolean) -> Unit,
    onUserClickEvent: (UserClickEvent) -> Unit
) {
    val listState = rememberLazyListState()

    // Calculate bottom padding based on FAB configuration
    // ExtendedFAB height ≈ 56dp + Transfer FAB height ≈ 56dp + spacing ≈ 16dp + extra margin ≈ 16dp
    val bottomPadding = if (accounts.size >= 2) 144.dp else 88.dp // Two FABs vs one FAB

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = spacing.l,
            end = spacing.l,
            top = spacing.s,
            bottom = bottomPadding // Prevent FAB overlap
        )
    ) {
        items(
            items = accounts,
            key = { account -> account.id }
        ) { account ->

            val onAccountClicked = { onNavigateToTransactionScreen(account.id) }
            val onUpdateAccountTypeClicked = { accountName: String, type: AccountType ->
                val updatedAccount = account.copy(name = accountName, type = type)
                onUserClickEvent(UserClickEvent.UpdateAccount(account = updatedAccount))
            }
            val onDeleteAccountClicked = { onUserClickEvent(UserClickEvent.DeleteAccount(account = account)) }

            AccountItem(
                name = account.name,
                balance = account.balance,
                type = account.type,
                onNavigateToAccountDetails = onAccountClicked,
                onUpdateAccountClicked = onUpdateAccountTypeClicked,
                onDeleteAccountClicked = onDeleteAccountClicked
            )
        }
    }

    // Track if user has actually scrolled - FABs visible until first scroll up
    val hasUserScrolled = listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
    val isCurrentlyScrollingDown = listState.isScrollingDown()

    // Custom behavior: FABs visible by default, hide when scrolling UP, show when scrolling DOWN
    onUserScrolling(listState.isScrollingDown())
}

@Preview
@Composable
private fun AccountsListPreview() {
    GetALifeTheme {
        Surface {
            AccountsList(
                accounts = listOf(
                    Account(
                        id = 1,
                        name = "Cash",
                        balance = Money(100.0),
                        type = AccountType.Cash,
                        listPosition = 0,
                        updatedAt = Clock.System.now(),
                        createdAt = Clock.System.now()
                    ),
                    Account(
                        id = 2,
                        name = "Bankaccount",
                        balance = Money(500.0),
                        type = AccountType.Checking,
                        listPosition = 1,
                        updatedAt = Clock.System.now(),
                        createdAt = Clock.System.now()
                    ),
                    Account(
                        id = 3,
                        name = "Creditcard",
                        balance = Money(-200.0),
                        type = AccountType.CreditCard,
                        listPosition = 2,
                        updatedAt = Clock.System.now(),
                        createdAt = Clock.System.now()
                    ),
                    Account(
                        id = 4,
                        name = "Savings",
                        balance = Money(0.0),
                        type = AccountType.Savings,
                        listPosition = 3,
                        updatedAt = Clock.System.now(),
                        createdAt = Clock.System.now()
                    )
                ),
                onNavigateToTransactionScreen = {},
                onUserScrolling = {},
                onUserClickEvent = {}
            )
        }
    }
}