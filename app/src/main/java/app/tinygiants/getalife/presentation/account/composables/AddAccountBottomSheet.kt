package app.tinygiants.getalife.presentation.account.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountBottomSheet(
    onConfirmClicked: (accountName: String, balance: Money, type: AccountType) -> Unit,
    onDismissRequest: () -> Unit = { }
) {
    var showAccountTypeDropdown by rememberSaveable { mutableStateOf(false) }

    var startingCredit by remember { mutableStateOf(Money(value = 0.00)) }

    var accountNameInput by rememberSaveable { mutableStateOf("") }
    var startingCreditUserInput by rememberSaveable { mutableStateOf(startingCredit.formattedMoney) }
    var accountTypeInput by rememberSaveable { mutableStateOf(AccountType.Unknown) }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(id = R.string.add_account),
                modifier = Modifier.padding(spacing.l),
                style = MaterialTheme.typography.titleMedium
            )
            OutlinedTextField(
                value = accountNameInput,
                onValueChange = { accountNameInput = it },
                label = { Text(stringResource(R.string.account_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.l, vertical = spacing.m)
            )
            OutlinedTextField(
                value = startingCreditUserInput,
                onValueChange = { newValue ->
                    startingCreditUserInput = newValue.replace(oldChar = ',', newChar = '.')
                    startingCredit = Money(value = startingCreditUserInput.toDoubleOrNull() ?: return@OutlinedTextField)
                },
                label = { Text(stringResource(R.string.balance)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.l, vertical = spacing.m)
                    .onFocusChanged { focusState ->
                        startingCreditUserInput = if (focusState.isFocused) "" else startingCredit.formattedMoney
                    }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (accountTypeInput == AccountType.Unknown) stringResource(R.string.chose_account_type) else accountTypeInput.name,
                    modifier = Modifier
                        .clickable { showAccountTypeDropdown = true }
                        .padding(spacing.l)
                )
                DropdownMenu(
                    expanded = showAccountTypeDropdown,
                    onDismissRequest = { showAccountTypeDropdown = false },
                    modifier = Modifier
                        .width(200.dp)
                ) {
                    AccountType.entries.forEach { type ->

                        if (type == AccountType.Unknown) return@forEach

                        DropdownMenuItem(
                            text = { Text(text = type.name) },
                            onClick = {
                                accountTypeInput = type
                                showAccountTypeDropdown = false
                            }
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Button(
                    onClick = {
                        onConfirmClicked(
                            accountNameInput,
                            startingCredit,
                            accountTypeInput
                        )
                        onDismissRequest()
                    },
                    enabled = accountNameInput.isNotBlank() && accountTypeInput != AccountType.Unknown,
                    modifier = Modifier.padding(spacing.default),
                ) {
                    Text(text = stringResource(id = R.string.save))
                }
            }
            Spacer(modifier = Modifier.height(spacing.xl))
        }
    }
}