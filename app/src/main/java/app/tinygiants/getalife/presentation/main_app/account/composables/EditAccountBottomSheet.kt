package app.tinygiants.getalife.presentation.main_app.account.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    var accountTypeInput by rememberSaveable { mutableStateOf(type ?: AccountType.Unknown) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = spacing.s)
                    .size(width = 32.dp, height = 4.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.l, vertical = spacing.s)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = if (accountName == null)
                    stringResource(id = R.string.add_account)
                else
                    stringResource(R.string.edit_account),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = spacing.l)
            )

            // Account Name Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(spacing.l)
            ) {
                TextField(
                    value = accountNameInput,
                    onValueChange = { accountNameInput = it },
                    label = {
                        Text(
                            stringResource(R.string.account_name),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(spacing.m)
                )
            }

            Spacer(modifier = Modifier.height(spacing.l))

            // Account Type Selection
            Box {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAccountTypeDropdown = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(spacing.l)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.l)
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.chose_account_type),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(spacing.xs))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (accountTypeInput == AccountType.Unknown)
                                        stringResource(R.string.chose_account_type)
                                    else accountTypeInput.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                DropdownMenu(
                    expanded = showAccountTypeDropdown,
                    onDismissRequest = { showAccountTypeDropdown = false },
                    modifier = Modifier.width(200.dp)
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

            Spacer(modifier = Modifier.height(spacing.xl))

            // Save Button
            Button(
                onClick = {
                    onConfirmClicked(accountNameInput, accountTypeInput)
                    onDismissRequest()
                },
                enabled = accountNameInput.isNotBlank() && accountTypeInput != AccountType.Unknown,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                shape = RoundedCornerShape(spacing.l),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.save),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Delete Button (if editing existing account)
            if (onDeleteAccountClicked != null) {
                Spacer(modifier = Modifier.height(spacing.m))

                Button(
                    onClick = {
                        onDeleteAccountClicked()
                        onDismissRequest()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp),
                    shape = RoundedCornerShape(spacing.l),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.delete_account, accountName ?: ""),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }

            // Bottom padding for navigation bar
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@PreviewLightDark
@Composable
private fun EditAccountBottomSheetPreview() {
    GetALifeTheme {
        Surface {
            EditAccountBottomSheet(
                accountName = "Mein Konto",
                type = AccountType.Checking,
                onConfirmClicked = { _, _ -> },
                onDeleteAccountClicked = { }
            )
        }
    }
}