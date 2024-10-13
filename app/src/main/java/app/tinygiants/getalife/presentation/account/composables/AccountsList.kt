package app.tinygiants.getalife.presentation.account.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.presentation.account.UserClickEvent
import app.tinygiants.getalife.presentation.shared_composables.isScrollingDown
import app.tinygiants.getalife.theme.spacing

@Composable
fun AccountsList(
    accounts: List<Account>,
    onNavigateToTransactionScreen: (accountId: Long) -> Unit,
    onUserScrolling: (Boolean) -> Unit,
    onUserClickEvent: (UserClickEvent) -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = spacing.l,
                vertical = spacing.s
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

    onUserScrolling(listState.isScrollingDown())
}