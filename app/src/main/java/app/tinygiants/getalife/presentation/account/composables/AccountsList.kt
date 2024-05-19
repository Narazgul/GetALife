package app.tinygiants.getalife.presentation.account.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.presentation.account.UserClickEvent
import app.tinygiants.getalife.theme.spacing

@Composable
fun AccountsList(
    accounts: List<Account>,
    categories: List<Category>,
    onUserClickEvent: (UserClickEvent) -> Unit
) {

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = spacing.large,
                vertical = spacing.small
            )
    ) {
        items(
            items = accounts,
            key = { account -> account.id }
        ) { account ->

            val onAddTransactionClicked = { amount: Money?,
                                            direction: TransactionDirection?,
                                            description: String?,
                                            transactionPartner: String?,
                                            category: Category?
                ->
                onUserClickEvent(
                    UserClickEvent.AddTransaction(
                        amount = amount,
                        account = account,
                        direction = direction,
                        description = description,
                        transactionPartner = transactionPartner,
                        category = category
                    )
                )
            }
            val onUpdateAccountTypeClicked = { accountName: String, balance: Money, type: AccountType ->
                val updatedAccount = account.copy(name = accountName, balance = balance, type = type)
                onUserClickEvent(UserClickEvent.UpdateAccount(account = updatedAccount))
            }
            val onDeleteAccountClicked = { onUserClickEvent(UserClickEvent.DeleteAccount(account = account)) }

            AccountItem(
                name = account.name,
                balance = account.balance,
                type = account.type,
                categories = categories,
                onUpdateAccountClicked = onUpdateAccountTypeClicked,
                onDeleteAccountClicked = onDeleteAccountClicked,
                onTransaction = onAddTransactionClicked
            )
        }
    }
}