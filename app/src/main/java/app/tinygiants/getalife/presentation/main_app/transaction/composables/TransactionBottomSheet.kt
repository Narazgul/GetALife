package app.tinygiants.getalife.presentation.main_app.transaction.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
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
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionBottomSheet(
    transaction: Transaction?,
    categories: List<Category>,
    accounts: List<Account>,
    onDismissRequest: () -> Unit,
    onSaveTransactionClicked: (
        amount: Money,
        account: Account,
        category: Category?,
        direction: TransactionDirection,
        description: Description,
        transactionPartner: TransactionPartner
    ) -> Unit,
    onDeleteTransactionClicked: () -> Unit = {},
) {

    var showCategoryDropdown by rememberSaveable { mutableStateOf(false) }
    var showAccountDropdown by rememberSaveable { mutableStateOf(false) }

    var amountMoney by remember { mutableStateOf(transaction?.amount ?: Money(value = 0.0)) }
    var amountUserInputText by rememberSaveable { mutableStateOf(amountMoney.toString()) }
    var descriptionUserInput by rememberSaveable { mutableStateOf(transaction?.description ?: "") }
    var transactionPartnerUserInput by rememberSaveable { mutableStateOf(transaction?.transactionPartner ?: "") }
    var transactionDirectionUserInput by rememberSaveable {
        mutableStateOf(transaction?.transactionDirection ?: TransactionDirection.Unknown)
    }
    var categoryUserInput by remember { mutableStateOf(transaction?.category) }
    var accountUserInput by remember { mutableStateOf(transaction?.account) }


    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // TODO Change TextField to Big Button or similar with animatedWaves as Background
            TextField(
                value = amountUserInputText,
                onValueChange = { userInput ->
                    amountUserInputText = userInput
                    amountMoney = Money(value = userInput.toDoubleOrNull() ?: amountMoney.asDouble())
                },
                label = { Text(stringResource(R.string.amount)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        amountUserInputText =
                            if (focusState.isFocused.not()) amountMoney.formattedMoney
                            else ""
                    }
            )

            Spacer(modifier = Modifier.height(spacing.l))

            MultiChoiceSegmentedButtonRow {
                SegmentedButton(
                    checked = transactionDirectionUserInput == TransactionDirection.Inflow,
                    onCheckedChange = { isChecked ->
                        transactionDirectionUserInput = if (isChecked) TransactionDirection.Inflow
                        else TransactionDirection.Outflow

                        // TODO change background color of animatedWaves
                    },
                    shape = RoundedCornerShape(topStart = spacing.l, bottomStart = spacing.l),
                ) {
                    Text(text = stringResource(R.string.inflow))
                }
                SegmentedButton(
                    checked = transactionDirectionUserInput == TransactionDirection.Outflow,
                    onCheckedChange = { isChecked ->
                        transactionDirectionUserInput = if (isChecked) TransactionDirection.Outflow
                        else TransactionDirection.Inflow

                        // TODO change background color of animatedWaves
                    },
                    shape = RoundedCornerShape(topEnd = spacing.l, bottomEnd = spacing.l),
                ) {
                    Text(text = stringResource(R.string.outflow))
                }
            }

            Spacer(modifier = Modifier.height(spacing.s))

            AnimatedVisibility(visible = transactionDirectionUserInput == TransactionDirection.Outflow) {
                ExposedDropdownMenuBox(
                    expanded = showCategoryDropdown,
                    onExpandedChange = { showCategoryDropdown = !showCategoryDropdown }
                ) {
                    TextField(
                        value = categoryUserInput?.name ?: "",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text(stringResource(id = R.string.chose_category)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = showCategoryDropdown,
                        onDismissRequest = { showCategoryDropdown = false }
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.name) },
                                onClick = {
                                    categoryUserInput = category
                                    showCategoryDropdown = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(spacing.m))
            }

            ExposedDropdownMenuBox(
                expanded = showAccountDropdown,
                onExpandedChange = { showAccountDropdown = !showAccountDropdown }
            ) {
                TextField(
                    value = accountUserInput?.name ?: "",
                    onValueChange = { },
                    readOnly = true,
                    label = { Text(stringResource(id = R.string.choose_account)) },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
                )
                ExposedDropdownMenu(
                    expanded = showAccountDropdown,
                    onDismissRequest = { showAccountDropdown = false }
                ) {
                    accounts.forEach { account ->
                        DropdownMenuItem(
                            text = { Text(account.name) },
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
                value = transactionPartnerUserInput,
                onValueChange = { userInput -> transactionPartnerUserInput = userInput },
                label = { Text(stringResource(R.string.transaction_partner)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(spacing.l))

            TextField(
                value = descriptionUserInput,
                onValueChange = { userInput -> descriptionUserInput = userInput },
                label = { Text(stringResource(R.string.description)) },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(spacing.l))

            Button(
                onClick = {
                    onSaveTransactionClicked(
                        amountMoney,
                        accountUserInput!!,
                        categoryUserInput,
                        transactionDirectionUserInput,
                        descriptionUserInput,
                        transactionPartnerUserInput
                    )
                    onDismissRequest()
                },
                enabled = accountUserInput != null &&
                        transactionDirectionUserInput != TransactionDirection.Unknown &&
                        (categoryUserInput != null || transactionDirectionUserInput == TransactionDirection.Inflow)
            ) {
                Text(text = stringResource(id = R.string.save))
            }

            Spacer(modifier = Modifier.height(spacing.l))

            if (transaction != null)
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