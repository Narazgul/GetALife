package app.tinygiants.getalife.presentation.main_app.transaction.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionBottomSheet(
    transaction: Transaction,
    accounts: List<Account>,
    categories: List<Category>,
    onExchangeTransactionDirectionClicked: (transaction: Transaction) -> Unit = {},
    onUpdateTransactionClicked: (transaction: Transaction) -> Unit = {},
    onExchangeAccountClicked: (transaction: Transaction, oldAccount: Account?) -> Unit = { _, _ -> },
    onExchangeCategoryClicked: (transaction: Transaction, oldCategory: Category?) -> Unit = { _, _ -> },
    onDeleteTransactionClicked: () -> Unit = {},
    onDismissRequest: () -> Unit = {}
) {

    var showCategoryDropdown by rememberSaveable { mutableStateOf(false) }
    var showAccountDropdown by rememberSaveable { mutableStateOf(false) }

    var amountMoney by remember { mutableStateOf(transaction.amount) }
    var amountUserInputText by rememberSaveable { mutableStateOf(amountMoney.toString()) }
    var descriptionUserInput by rememberSaveable { mutableStateOf(transaction.description) }
    var transactionPartnerUserInput by rememberSaveable { mutableStateOf(transaction.transactionPartner) }
    var directionUserInput by rememberSaveable { mutableStateOf(transaction.transactionDirection) }
    var categoryUserInput by remember { mutableStateOf(transaction.category) }
    var accountUserInput by remember { mutableStateOf(transaction.account) }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            MultiChoiceSegmentedButtonRow {
                SegmentedButton(
                    checked = directionUserInput == TransactionDirection.Inflow,
                    onCheckedChange = { isChecked ->
                        directionUserInput = if (isChecked) TransactionDirection.Inflow
                        else TransactionDirection.Outflow

                        onExchangeTransactionDirectionClicked(transaction.copy(transactionDirection = directionUserInput))
                    },
                    shape = RoundedCornerShape(topStart = spacing.l, bottomStart = spacing.l),
                ) {
                    Text(text = "Inflow")
                }
                SegmentedButton(
                    checked = directionUserInput == TransactionDirection.Outflow,
                    onCheckedChange = { isChecked ->
                        directionUserInput = if (isChecked) TransactionDirection.Outflow
                        else TransactionDirection.Inflow

                        onExchangeTransactionDirectionClicked(transaction.copy(transactionDirection = directionUserInput))
                    },
                    shape = RoundedCornerShape(topEnd = spacing.l, bottomEnd = spacing.l),
                ) {
                    Text(text = "Outflow")
                }
            }
            Spacer(modifier = Modifier.height(spacing.s))
            AnimatedVisibility(visible = directionUserInput == TransactionDirection.Outflow) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = categoryUserInput?.name ?: stringResource(id = R.string.chose_category),
                        modifier = Modifier
                            .clickable { showCategoryDropdown = true }
                            .padding(spacing.s)
                    )
                    DropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false },
                        modifier = Modifier
                            .width(200.dp)
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(text = category.name) },
                                onClick = {
                                    categoryUserInput = category
                                    showCategoryDropdown = false

                                    onExchangeCategoryClicked(transaction.copy(category = category), transaction.category)
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(spacing.m))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = accountUserInput.name,
                    modifier = Modifier
                        .clickable { showAccountDropdown = true }
                        .padding(spacing.s)
                )
                DropdownMenu(
                    expanded = showAccountDropdown,
                    onDismissRequest = { showAccountDropdown = false },
                    modifier = Modifier
                        .width(200.dp)
                ) {
                    accounts.forEach { account ->
                        DropdownMenuItem(
                            text = { Text(text = account.name) },
                            onClick = {
                                accountUserInput = account
                                showAccountDropdown = false

                                onExchangeAccountClicked(transaction.copy(account = account), transaction.account)
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(spacing.l))
            TextField(
                value = amountUserInputText,
                onValueChange = { userInput ->
                    amountUserInputText = userInput
                    amountMoney = Money(value = userInput.toDoubleOrNull() ?: amountMoney.asDouble())

                    onUpdateTransactionClicked(transaction.copy(amount = amountMoney))
                },
                label = { Text(stringResource(R.string.amount)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { amountUserInputText = if (it.isFocused.not()) amountMoney.formattedMoney else "" }
            )
            Spacer(modifier = Modifier.height(spacing.l))
            TextField(
                value = transactionPartnerUserInput,
                onValueChange = { userInput ->
                    transactionPartnerUserInput = userInput
                    onUpdateTransactionClicked(transaction.copy(transactionPartner = transactionPartnerUserInput))
                },
                label = { Text(stringResource(R.string.transaction_partner)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(spacing.l))
            TextField(
                value = descriptionUserInput,
                onValueChange = { userInput ->
                    descriptionUserInput = userInput
                    onUpdateTransactionClicked(transaction.copy(description = descriptionUserInput))
                },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(spacing.l))
            Button(
                onClick = { onDeleteTransactionClicked() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(
                    text = stringResource(id = R.string.delete_transaction),
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun TransactionDialogPreview() {
    GetALifeTheme {
        Surface {
            EditTransactionBottomSheet(
                transaction = Transaction(
                    id = 1L,
                    amount = Money(value = 1000.00),
                    account = Account(
                        id = 1L,
                        name = "Cash",
                        balance = EmptyMoney(),
                        type = AccountType.Cash,
                        listPosition = 0,
                        updatedAt = Clock.System.now(),
                        createdAt = Clock.System.now(),
                    ),
                    category = null,
                    transactionPartner = "Landlord",
                    transactionDirection = TransactionDirection.Unknown,
                    description = "Rent for Mai",
                    dateOfTransaction = Clock.System.now(),
                    updatedAt = Clock.System.now(),
                    createdAt = Clock.System.now()
                ),
                accounts = emptyList(),
                categories = emptyList()
            )
        }
    }
}