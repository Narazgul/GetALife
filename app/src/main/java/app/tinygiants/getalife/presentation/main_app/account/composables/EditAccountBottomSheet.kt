package app.tinygiants.getalife.presentation.main_app.account.composables

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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAccountBottomSheet(
    accountName: String? = null,
    type: AccountType? = null,
    onConfirmClicked: (accountName: String, type: AccountType) -> Unit,
    onDeleteAccountClicked: (() -> Unit)? = null,
    onDismissRequest: () -> Unit = { }
) {

    var showAccountTypeDropdown by rememberSaveable { mutableStateOf(false) }

    var accountNameInput by rememberSaveable { mutableStateOf(accountName ?: "") }
    var accountTypeInput by rememberSaveable { mutableStateOf(type) }

    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = if (accountName == null) stringResource(id = R.string.add_account) else stringResource(R.string.edit_account),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = accountTypeInput?.name ?: stringResource(R.string.chose_account_type),
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
                        onClick = {
                            onDeleteAccountClicked()
                            onDismissRequest()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(
                            text = stringResource(id = R.string.delete_account, accountName ?: ""),
                            color = MaterialTheme.colorScheme.onError
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
                            accountTypeInput ?: AccountType.Unknown
                        )
                        onDismissRequest()
                    },
                    modifier = Modifier.padding(spacing.m),
                ) {
                    Text(text = stringResource(id = R.string.save))
                }
            }
            Spacer(modifier = Modifier.height(spacing.xl))
        }
    }
}

@PreviewLightDark
@Composable
private fun AccountDialogPreview() {
    GetALifeTheme {
        Surface {
            EditAccountBottomSheet(onConfirmClicked = { _, _ -> })
        }
    }
}