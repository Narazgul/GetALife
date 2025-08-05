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
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferBetweenAccountsBottomSheet(
    accounts: List<Account>,
    onTransferClicked: (fromAccount: Account, toAccount: Account, amount: Money, description: String) -> Unit,
    onDismissRequest: () -> Unit = { }
) {
    var showFromAccountDropdown by rememberSaveable { mutableStateOf(false) }
    var showToAccountDropdown by rememberSaveable { mutableStateOf(false) }

    var transferAmount by remember { mutableStateOf(Money(value = 0.0)) }
    var amountUserInput by rememberSaveable { mutableStateOf("") }
    var descriptionInput by rememberSaveable { mutableStateOf("") }

    var fromAccount by remember { mutableStateOf<Account?>(accounts.firstOrNull()) }
    var toAccount by remember { mutableStateOf<Account?>(accounts.getOrNull(1)) }

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
            // Title with Transfer Icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(spacing.s))
                Text(
                    text = "Geld zwischen Konten übertragen",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(spacing.l))

            // 2x2 Grid for From Account, To Account, Amount, Description
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing.m)
                ) {
                    // From Account Selection
                    Box(modifier = Modifier.weight(1f)) {
                        SelectionCard(
                            label = "Von Konto",
                            selectedText = fromAccount?.name ?: "Konto wählen",
                            onClick = { showFromAccountDropdown = true },
                            isCompact = true
                        )
                        DropdownMenu(
                            expanded = showFromAccountDropdown,
                            onDismissRequest = { showFromAccountDropdown = false },
                            modifier = Modifier.width(200.dp)
                        ) {
                            accounts.forEach { account ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(text = account.name)
                                            Text(
                                                text = account.balance.formattedMoney,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        fromAccount = account
                                        showFromAccountDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    // To Account Selection
                    Box(modifier = Modifier.weight(1f)) {
                        SelectionCard(
                            label = "Zu Konto",
                            selectedText = toAccount?.name ?: "Konto wählen",
                            onClick = { showToAccountDropdown = true },
                            isCompact = true
                        )
                        DropdownMenu(
                            expanded = showToAccountDropdown,
                            onDismissRequest = { showToAccountDropdown = false },
                            modifier = Modifier.width(200.dp)
                        ) {
                            accounts.filter { it.id != fromAccount?.id }.forEach { account ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(text = account.name)
                                            Text(
                                                text = account.balance.formattedMoney,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        toAccount = account
                                        showToAccountDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(spacing.m))

                // Amount Input - Full Width
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(spacing.l)
                ) {
                    TextField(
                        value = amountUserInput,
                        onValueChange = { userInput ->
                            amountUserInput = userInput.replace(oldChar = ',', newChar = '.')
                            transferAmount = Money(value = amountUserInput.toDoubleOrNull() ?: 0.0)
                        },
                        label = {
                            Text(
                                "Betrag",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged {
                                amountUserInput = if (it.isFocused.not()) transferAmount.formattedMoney
                                else ""
                            },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(spacing.m)
                    )
                }
            }

            Spacer(modifier = Modifier.height(spacing.l))

            // Description Input - Full Width
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(spacing.l)
            ) {
                TextField(
                    value = descriptionInput,
                    onValueChange = { descriptionInput = it },
                    label = {
                        Text(
                            "Beschreibung (optional)",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(spacing.m)
                )
            }

            Spacer(modifier = Modifier.height(spacing.xl))

            // Transfer Button
            Button(
                onClick = {
                    if (fromAccount != null && toAccount != null) {
                        onTransferClicked(
                            fromAccount!!,
                            toAccount!!,
                            transferAmount,
                            descriptionInput.ifEmpty { "Account Transfer" }
                        )
                        onDismissRequest()
                    }
                },
                enabled = fromAccount != null &&
                        toAccount != null &&
                        fromAccount?.id != toAccount?.id &&
                        transferAmount.asDouble() > 0,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                shape = RoundedCornerShape(spacing.l),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(spacing.s))
                Text(
                    text = "Übertragen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Bottom padding for navigation bar
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun SelectionCard(
    label: String,
    selectedText: String,
    onClick: () -> Unit,
    isCompact: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .then(if (!isCompact) Modifier.fillMaxWidth() else Modifier)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(spacing.l)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isCompact) spacing.m else spacing.l)
        ) {
            Column {
                Text(
                    text = label,
                    style = if (isCompact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(spacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedText,
                        style = if (isCompact) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(if (isCompact) 20.dp else 24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}