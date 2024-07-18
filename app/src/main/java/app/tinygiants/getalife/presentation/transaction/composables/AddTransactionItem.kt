package app.tinygiants.getalife.presentation.transaction.composables

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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing

typealias Description = String
typealias TransactionPartner = String

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionItem(
    categories: List<Category>,
    accounts: List<Account>,
    onAddTransactionClicked: (
        amount: Money,
        account: Account?,
        category: Category?,
        direction: TransactionDirection,
        description: Description,
        transactionPartner: TransactionPartner
    ) -> Unit,
    modifier: Modifier = Modifier
) {

    var showCategoryDropdown by rememberSaveable { mutableStateOf(false) }
    var showAccountDropdown by rememberSaveable { mutableStateOf(false) }

    var amountMoney by remember { mutableStateOf(Money(value = 0.0)) }
    var amountUserInputText by rememberSaveable { mutableStateOf("") }
    var descriptionUserInput by rememberSaveable { mutableStateOf("") }
    var transactionPartnerUserInput by rememberSaveable { mutableStateOf("") }
    var directionUserInput by rememberSaveable { mutableStateOf(TransactionDirection.Unknown) }
    var categoryUserInput by remember { mutableStateOf(categories.firstOrNull()) }
    var accountUserInput by remember { mutableStateOf(accounts.firstOrNull()) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        MultiChoiceSegmentedButtonRow {
            SegmentedButton(
                checked = directionUserInput == TransactionDirection.Inflow,
                onCheckedChange = { isChecked ->
                    directionUserInput = if (isChecked) TransactionDirection.Inflow
                    else TransactionDirection.Unknown
                },
                shape = RoundedCornerShape(topStart = spacing.l, bottomStart = spacing.l),
            ) {
                Text(text = stringResource(R.string.inflow))
            }
            SegmentedButton(
                checked = directionUserInput == TransactionDirection.Outflow,
                onCheckedChange = { isChecked ->
                    directionUserInput = if (isChecked) TransactionDirection.Outflow
                    else TransactionDirection.Unknown
                },
                shape = RoundedCornerShape(topEnd = spacing.l, bottomEnd = spacing.l),
            ) {
                Text(text = stringResource(R.string.outflow))
            }
        }
        Spacer(modifier = Modifier.height(spacing.s))
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
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(spacing.default))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = accountUserInput?.name ?: stringResource(id = R.string.choose_account),
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
                amountMoney = Money(
                    value = userInput.toDoubleOrNull() ?: amountMoney.value
                )
            },
            label = { Text(stringResource(R.string.amount)) },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    amountUserInputText = if (it.isFocused.not()) amountMoney.formattedMoney
                    else amountMoney.value.toString()
                }
        )
        Spacer(modifier = Modifier.height(spacing.l))
        TextField(
            value = descriptionUserInput,
            onValueChange = { userInput -> descriptionUserInput = userInput },
            label = { Text(stringResource(R.string.description)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(spacing.l))
        TextField(
            value = transactionPartnerUserInput,
            onValueChange = { userInput -> transactionPartnerUserInput = userInput },
            label = { Text(stringResource(R.string.transaction_partner)) },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(spacing.xl))
        Button(
            onClick = {
                onAddTransactionClicked(
                    amountMoney,
                    accountUserInput,
                    categoryUserInput!!,
                    directionUserInput,
                    descriptionUserInput,
                    transactionPartnerUserInput,
                )
            },
            enabled = accountUserInput != null && categoryUserInput != null
        ) {
            Text(text = stringResource(id = R.string.save))
        }
    }
}

@Preview
@Composable
private fun EnterTransactionPreview() {
    GetALifeTheme {
        Surface {
            AddTransactionItem(
                categories = emptyList(),
                accounts = emptyList(),
                onAddTransactionClicked = { _, _, _, _, _, _ -> })
        }
    }
}