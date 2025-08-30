package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.presentation.shared_composables.InputValidationUtils
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.onSuccess
import app.tinygiants.getalife.theme.spacing
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDate
import kotlin.time.Instant

/**
 * Unified input components for all transaction flows.
 * These components work in both Guided and Standard modes.
 */

// ================================
// Flow Selection Component
// ================================

@Composable
fun TransactionFlowSelector(
    selectedDirection: TransactionDirection?,
    onDirectionSelected: (TransactionDirection) -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    accounts: List<Account> = emptyList()
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.m)
    ) {
        if (!isCompact) {
            Text(
                text = "Was möchten Sie tun?",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        MultiChoiceSegmentedButtonRow {
            SegmentedButton(
                checked = selectedDirection == TransactionDirection.Inflow,
                onCheckedChange = {
                    if (it) onDirectionSelected(TransactionDirection.Inflow)
                },
                shape = RoundedCornerShape(topStart = spacing.l, bottomStart = spacing.l)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💰", style = MaterialTheme.typography.headlineSmall)
                    Text("Einnahme", style = MaterialTheme.typography.bodySmall)
                }
            }

            // Only show Transfer if there are at least 2 accounts
            if (accounts.size >= 2) {
                SegmentedButton(
                    checked = selectedDirection == TransactionDirection.AccountTransfer,
                    onCheckedChange = {
                        if (it) onDirectionSelected(TransactionDirection.AccountTransfer)
                    },
                    shape = RoundedCornerShape(0.dp) // Middle button has no rounded corners
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔄", style = MaterialTheme.typography.headlineSmall)
                        Text("Transfer", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            SegmentedButton(
                checked = selectedDirection == TransactionDirection.Outflow,
                onCheckedChange = {
                    if (it) onDirectionSelected(TransactionDirection.Outflow)
                },
                shape = if (accounts.size >= 2) {
                    RoundedCornerShape(topEnd = spacing.l, bottomEnd = spacing.l)
                } else {
                    RoundedCornerShape(topEnd = spacing.l, bottomEnd = spacing.l)
                }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💸", style = MaterialTheme.typography.headlineSmall)
                    Text("Ausgabe", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

// ================================
// Amount Input Component
// ================================

@Composable
fun TransactionAmountInput(
    amount: Money?,
    onAmountChanged: (Money) -> Unit,
    transactionDirection: TransactionDirection?,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    var amountText by rememberSaveable { mutableStateOf(amount?.asDouble()?.toString() ?: "") }
    val focusManager = LocalFocusManager.current

    val title = when (transactionDirection) {
        TransactionDirection.Inflow -> "Wie viel haben Sie erhalten?"
        TransactionDirection.Outflow -> "Wie viel haben Sie ausgegeben?"
        TransactionDirection.AccountTransfer -> "Wie viel möchten Sie überweisen?"
        else -> "Betrag eingeben"
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.m)
    ) {
        if (!isCompact) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            shape = RoundedCornerShape(spacing.l)
        ) {
            TextField(
                value = amountText,
                onValueChange = { input ->
                    amountText = input.replace(',', '.')
                    val parsedAmount = InputValidationUtils.parseAmountInput(
                        amountText,
                        amount ?: Money(0.0)
                    )
                    onAmountChanged(parsedAmount)
                },
                label = { Text("Betrag (€)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { focusState ->
                        if (!focusState.isFocused && amount != null) {
                            amountText = amount.formattedMoney
                        } else if (focusState.isFocused) {
                            amountText = amount?.asDouble()?.toString() ?: ""
                        }
                    },
                textStyle = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(spacing.l)
            )
        }
    }
}

// ================================
// Account Selection Component
// ================================

@Composable
fun TransactionAccountSelector(
    accounts: List<Account>,
    selectedAccount: Account?,
    onAccountSelected: (Account) -> Unit,
    onCreateAccountClicked: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false,
    excludeAccount: Account? = null // For transfers - exclude the source account
) {
    val availableAccounts = if (excludeAccount != null) {
        accounts.filter { it.id != excludeAccount.id }
    } else {
        accounts
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.m)
    ) {
        if (!isCompact) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(spacing.s),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(availableAccounts) { account ->
                AccountSelectionCard(
                    account = account,
                    isSelected = selectedAccount?.id == account.id,
                    onClick = { onAccountSelected(account) }
                )
            }

            item {
                OutlinedButton(
                    onClick = onCreateAccountClicked,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(spacing.s))
                    Text("Neues Konto erstellen")
                }
            }
        }
    }
}

@Composable
private fun AccountSelectionCard(
    account: Account,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        ),
        shape = RoundedCornerShape(spacing.m)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.m),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = getAccountTypeEmoji(account.type) + " " + account.type.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = account.balance.formattedMoney,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (account.balance.asDouble() >= 0) {
                    onSuccess
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

private fun getAccountTypeEmoji(type: AccountType): String = when (type) {
    AccountType.Cash -> "💵"
    AccountType.Checking -> "🏦"
    AccountType.Savings -> "💰"
    AccountType.CreditCard -> "💳"
    AccountType.Mortgage -> "🏠"
    AccountType.Loan -> "📋"
    AccountType.Depot -> "📈"
    AccountType.Unknown -> "❓"
}

// ================================
// Category Selection Component
// ================================

@Composable
fun TransactionCategorySelector(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit,
    onCreateCategoryClicked: () -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing.m)
    ) {
        if (!isCompact) {
            Text(
                text = "Für welche Kategorie?",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(spacing.s),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                CategorySelectionCard(
                    category = category,
                    isSelected = selectedCategory?.id == category.id,
                    onClick = { onCategorySelected(category) }
                )
            }

            item {
                OutlinedButton(
                    onClick = onCreateCategoryClicked,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(spacing.s))
                    Text("Neue Kategorie erstellen")
                }
            }
        }
    }
}

@Composable
private fun CategorySelectionCard(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        ),
        shape = RoundedCornerShape(spacing.m)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(spacing.m),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.emoji.ifEmpty { "📂" },
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.width(spacing.m))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Budget: ${category.budgetTarget.formattedMoney}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ================================
// Partner Input Component
// ================================

@Composable
fun TransactionPartnerInput(
    partner: String,
    onPartnerChanged: (String) -> Unit,
    transactionDirection: TransactionDirection?,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val title = when (transactionDirection) {
        TransactionDirection.Inflow -> "Von wem haben Sie Geld erhalten?"
        TransactionDirection.Outflow -> "Wo haben Sie das Geld ausgegeben?"
        else -> "Partner eingeben"
    }

    val placeholder = when (transactionDirection) {
        TransactionDirection.Inflow -> "z.B. Arbeitgeber, Kunde..."
        TransactionDirection.Outflow -> "z.B. Supermarkt, Restaurant..."
        else -> "Name des Partners"
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.m)
    ) {
        if (!isCompact) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(spacing.l)
        ) {
            TextField(
                value = partner,
                onValueChange = onPartnerChanged,
                label = { Text("Partner") },
                placeholder = { Text(placeholder) },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(spacing.l)
            )
        }
    }
}

// ================================
// Date Selection Component
// ================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDateSelector(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.toEpochDay()?.times(24 * 60 * 60 * 1000)
    )
    val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.m)
    ) {
        if (!isCompact) {
            Text(
                text = "Wann war das?",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(spacing.l)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.l),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Datum",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = selectedDate?.let { formatDate(it) } ?: "Heute",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val instant = Instant.fromEpochMilliseconds(millis)
                            val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
                            onDateSelected(LocalDate.of(localDate.year, localDate.month.number, localDate.dayOfMonth))
                        }
                        showDatePicker = false
                    },
                    enabled = confirmEnabled.value
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Abbrechen")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

private fun formatDate(date: LocalDate): String {
    return "${date.dayOfMonth.toString().padStart(2, '0')}.${
        date.monthValue.toString().padStart(2, '0')
    }.${date.year}"
}

// ================================
// Description Input Component
// ================================

@Composable
fun TransactionDescriptionInput(
    description: String,
    onDescriptionChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.m)
    ) {
        if (!isCompact) {
            Text(
                text = "Möchten Sie eine Notiz hinzufügen?",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(spacing.l)
        ) {
            TextField(
                value = description,
                onValueChange = onDescriptionChanged,
                label = { Text("Beschreibung (optional)") },
                placeholder = { Text("Weitere Details zur Transaktion...") },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                maxLines = 3,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Done
                ),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(spacing.l)
            )
        }
    }
}

// ================================
// Preview Composables
// ================================

@Preview(name = "Flow Selector", showBackground = true)
@Composable
private fun TransactionFlowSelectorPreview() {
    GetALifeTheme {
        // Only two dummy accounts so Transfer is shown in preview
        val dummyAccounts = listOf(
            Account(
                id = 1L,
                name = "Konto 1",
                type = AccountType.Checking,
                balance = Money(100.0),
                listPosition = 0,
                updatedAt = kotlin.time.Clock.System.now(),
                createdAt = kotlin.time.Clock.System.now()
            ),
            Account(
                id = 2L,
                name = "Konto 2",
                type = AccountType.Savings,
                balance = Money(200.0),
                listPosition = 1,
                updatedAt = kotlin.time.Clock.System.now(),
                createdAt = kotlin.time.Clock.System.now()
            )
        )
        TransactionFlowSelector(
            selectedDirection = TransactionDirection.Outflow,
            onDirectionSelected = { },
            modifier = Modifier.padding(16.dp),
            accounts = dummyAccounts
        )
    }
}

@Preview(name = "Amount Input", showBackground = true)
@Composable
private fun TransactionAmountInputPreview() {
    GetALifeTheme {
        TransactionAmountInput(
            amount = Money(25.50),
            onAmountChanged = { },
            transactionDirection = TransactionDirection.Outflow,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Account Selector", showBackground = true)
@Composable
private fun TransactionAccountSelectorPreview() {
    GetALifeTheme {
        val dummyAccounts = listOf(
            Account(
                id = 1L,
                name = "Hauptkonto",
                type = AccountType.Checking,
                balance = Money(1250.75),
                listPosition = 0,
                updatedAt = kotlin.time.Clock.System.now(),
                createdAt = kotlin.time.Clock.System.now()
            ),
            Account(
                id = 2L,
                name = "Sparkonto",
                type = AccountType.Savings,
                balance = Money(5000.0),
                listPosition = 1,
                updatedAt = kotlin.time.Clock.System.now(),
                createdAt = kotlin.time.Clock.System.now()
            ),
            Account(
                id = 3L,
                name = "Bargeld",
                type = AccountType.Cash,
                balance = Money(-50.25),
                listPosition = 2,
                updatedAt = kotlin.time.Clock.System.now(),
                createdAt = kotlin.time.Clock.System.now()
            )
        )
        TransactionAccountSelector(
            accounts = dummyAccounts,
            selectedAccount = dummyAccounts[0],
            onAccountSelected = { },
            onCreateAccountClicked = { },
            title = "Von welchem Konto?",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Category Selector", showBackground = true)
@Composable
private fun TransactionCategorySelectorPreview() {
    GetALifeTheme {
        val dummyCategories = listOf(
            Category(
                id = 1L,
                groupId = 1L,
                emoji = "🍕",
                name = "Lebensmittel",
                budgetTarget = Money(400.0),
                monthlyTargetAmount = null,
                targetMonthsRemaining = null,
                listPosition = 0,
                isInitialCategory = false,
                linkedAccountId = null,
                updatedAt = kotlin.time.Clock.System.now(),
                createdAt = kotlin.time.Clock.System.now()
            ),
            Category(
                id = 2L,
                groupId = 1L,
                emoji = "🚗",
                name = "Transport",
                budgetTarget = Money(200.0),
                monthlyTargetAmount = null,
                targetMonthsRemaining = null,
                listPosition = 1,
                isInitialCategory = false,
                linkedAccountId = null,
                updatedAt = kotlin.time.Clock.System.now(),
                createdAt = kotlin.time.Clock.System.now()
            ),
            Category(
                id = 3L,
                groupId = 2L,
                emoji = "🎮",
                name = "Unterhaltung",
                budgetTarget = Money(150.0),
                monthlyTargetAmount = null,
                targetMonthsRemaining = null,
                listPosition = 2,
                isInitialCategory = false,
                linkedAccountId = null,
                updatedAt = kotlin.time.Clock.System.now(),
                createdAt = kotlin.time.Clock.System.now()
            )
        )
        TransactionCategorySelector(
            categories = dummyCategories,
            selectedCategory = dummyCategories[1],
            onCategorySelected = { },
            onCreateCategoryClicked = { },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Partner Input - Inflow", showBackground = true)
@Composable
private fun TransactionPartnerInputInflowPreview() {
    GetALifeTheme {
        TransactionPartnerInput(
            partner = "Mein Arbeitgeber",
            onPartnerChanged = { },
            transactionDirection = TransactionDirection.Inflow,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Partner Input - Outflow", showBackground = true)
@Composable
private fun TransactionPartnerInputOutflowPreview() {
    GetALifeTheme {
        TransactionPartnerInput(
            partner = "Edeka Supermarkt",
            onPartnerChanged = { },
            transactionDirection = TransactionDirection.Outflow,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Date Selector", showBackground = true)
@Composable
private fun TransactionDateSelectorPreview() {
    GetALifeTheme {
        TransactionDateSelector(
            selectedDate = LocalDate.of(2024, 3, 15),
            onDateSelected = { },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Description Input", showBackground = true)
@Composable
private fun TransactionDescriptionInputPreview() {
    GetALifeTheme {
        TransactionDescriptionInput(
            description = "Wocheneinkauf mit Familie",
            onDescriptionChanged = { },
            modifier = Modifier.padding(16.dp)
        )
    }
}