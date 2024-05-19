package app.tinygiants.getalife.presentation.account.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing

@Composable
fun AccountDialog(
    accountName: String? = null,
    balance: Money? = null,
    type: AccountType? = null,
    onConfirmClicked: (accountName: String, balance: Money, type: AccountType) -> Unit,
    onDeleteAccountClicked: (() -> Unit)? = null,
    onDismissRequest: () -> Unit = { }
) {

    var showAccountTypeDropdown by rememberSaveable { mutableStateOf(false) }

    var accountNameInput by rememberSaveable { mutableStateOf(accountName ?: "") }
    var balanceInput by rememberSaveable { mutableDoubleStateOf(balance?.value ?: 0.00) }
    var accountTypeInput by rememberSaveable { mutableStateOf(type ?: AccountType.Unknown) }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(450.dp)
                .padding(spacing.large),
            shape = RoundedCornerShape(spacing.large),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = if (accountName == null) "Account hinzufügen" else "Account bearbeiten",
                    modifier = Modifier.padding(spacing.large),
                    style = MaterialTheme.typography.titleMedium
                )
                OutlinedTextField(
                    value = accountNameInput,
                    onValueChange = { accountNameInput = it },
                    label = { Text("Account Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.large, vertical = spacing.medium)
                )
                OutlinedTextField(
                    value = balanceInput.toString(),
                    onValueChange = { newValue -> balanceInput = newValue.toDoubleOrNull() ?: 0.00 },
                    label = { Text("Balance") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.large, vertical = spacing.medium)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = accountTypeInput.name,
                        modifier = Modifier
                            .clickable { showAccountTypeDropdown = true }
                            .padding(spacing.large)
                    )
                    DropdownMenu(
                        expanded = showAccountTypeDropdown,
                        onDismissRequest = { showAccountTypeDropdown = false },
                        modifier = Modifier
                            .width(200.dp)
                    ) {
                        AccountType.entries.forEach { type ->
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
                if (onDeleteAccountClicked != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { onDeleteAccountClicked() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text(
                                text = "$accountName löschen",
                                color = MaterialTheme.colorScheme.onError
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(spacing.default),
                    ) {
                        Text("Dismiss")
                    }
                    TextButton(
                        onClick = {
                            onConfirmClicked(
                                accountNameInput,
                                Money(value = balanceInput),
                                accountTypeInput
                            )
                            onDismissRequest()
                        },
                        modifier = Modifier.padding(spacing.default),
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AccountDialogPreview() {
    GetALifeTheme {
        Surface {
            AccountDialog(onConfirmClicked = { _, _, _-> })
        }
    }
}