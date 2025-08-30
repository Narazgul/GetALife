package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.presentation.shared_composables.InputValidationUtils
import app.tinygiants.getalife.theme.GetALifeTheme

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onCategoryCreated: (String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    val isValid = name.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Neue Kategorie erstellen", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name der Kategorie") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = { if (isValid) onCategoryCreated(name) }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
                Text(
                    text = "Das passende Emoji wird automatisch hinzugefügt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onCategoryCreated(name)
                    }
                },
                enabled = isValid
            ) {
                Text("Erstellen")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Abbrechen")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onAccountCreated: (String, Money, AccountType) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var balanceInput by rememberSaveable { mutableStateOf("0") }
    var selectedAccountType by rememberSaveable { mutableStateOf(AccountType.Checking) }
    var showAccountTypeDropdown by rememberSaveable { mutableStateOf(false) }

    val isValid = name.isNotBlank() && selectedAccountType != AccountType.Unknown
    val balance = InputValidationUtils.parseBalanceInput(balanceInput)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Neues Konto erstellen", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Kontoname") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = balanceInput,
                    onValueChange = { newValue ->
                        val filteredValue = newValue.replace(',', '.')
                            .filter { char -> char.isDigit() || char == '.' || char == '-' }
                        if (filteredValue.count { it == '.' } <= 1) {
                            balanceInput = filteredValue
                        }
                    },
                    label = { Text("Startbetrag (€)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (isValid) onAccountCreated(name, Money(balance), selectedAccountType)
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Box {
                    OutlinedTextField(
                        value = when (selectedAccountType) {
                            AccountType.Unknown -> "Kontotyp wählen"
                            AccountType.Cash -> "💵 Bargeld"
                            AccountType.Checking -> "🏦 Girokonto"
                            AccountType.Savings -> "💰 Sparkonto"
                            AccountType.CreditCard -> "💳 Kreditkarte"
                            AccountType.Mortgage -> "🏠 Hypothek"
                            AccountType.Loan -> "📋 Darlehen"
                            AccountType.Depot -> "📈 Depot"
                        },
                        onValueChange = { },
                        label = { Text("Kontotyp") },
                        singleLine = true,
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.clickable { showAccountTypeDropdown = !showAccountTypeDropdown }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAccountTypeDropdown = !showAccountTypeDropdown }
                    )

                    DropdownMenu(
                        expanded = showAccountTypeDropdown,
                        onDismissRequest = { showAccountTypeDropdown = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val accountTypes = listOf(
                            AccountType.Checking to "🏦 Girokonto",
                            AccountType.Cash to "💵 Bargeld",
                            AccountType.Savings to "💰 Sparkonto",
                            AccountType.CreditCard to "💳 Kreditkarte",
                            AccountType.Depot to "📈 Depot"
                        )

                        accountTypes.forEach { (type, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedAccountType = type
                                    showAccountTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "Der Startbetrag wird als erste Transaktion hinzugefügt",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (isValid) {
                        onAccountCreated(name, Money(balance), selectedAccountType)
                    }
                },
                enabled = isValid
            ) {
                Text("Erstellen")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Abbrechen")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Preview(name = "Add Category Dialog", showBackground = true)
@Composable
private fun AddCategoryDialogPreview() {
    GetALifeTheme {
        AddCategoryDialog(
            onDismiss = { },
            onCategoryCreated = { }
        )
    }
}