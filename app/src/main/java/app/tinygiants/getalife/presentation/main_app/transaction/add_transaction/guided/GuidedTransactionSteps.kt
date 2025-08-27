package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.model.categorization.NewCategorySuggestion
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.AddTransactionUiState
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.GuidedTransactionStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.SmartCategorizationUiState
import app.tinygiants.getalife.theme.onSuccess
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Clock

/**
 * Step 1: Transaction type selection (Inflow, Outflow, Transfer)
 * Performance optimized - receives ViewModel as parameter instead of injection
 */
@Composable
fun TransactionTypeStep(
    selectedDirection: TransactionDirection?,
    availableAccounts: List<Account>, // For determining if Transfer should be shown
    onTypeSelected: (TransactionDirection) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Was für eine Transaktion ist das?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        val options = buildList {
            add(TransactionDirection.Inflow to "💰 Einnahme")
            // Only show Transfer if there are at least 2 accounts
            if (availableAccounts.size >= 2) {
                add(TransactionDirection.Unknown to "↔️ Transfer")
            }
            add(TransactionDirection.Outflow to "💸 Ausgabe")
        }

        MultiChoiceSegmentedButtonRow {
            options.forEachIndexed { idx, (direction, label) ->
                SegmentedButton(
                    checked = direction == selectedDirection,
                    onCheckedChange = {
                        // Always call onTypeSelected regardless of checked state
                        // This allows re-selection of the same option
                        onTypeSelected(direction)
                    },
                    shape = when {
                        options.size == 1 -> RoundedCornerShape(16.dp)
                        idx == 0 -> RoundedCornerShape(
                            topStart = 16.dp,
                            bottomStart = 16.dp,
                            topEnd = 0.dp,
                            bottomEnd = 0.dp
                        )

                        idx == options.lastIndex -> RoundedCornerShape(
                            topEnd = 16.dp,
                            bottomEnd = 16.dp,
                            topStart = 0.dp,
                            bottomStart = 0.dp
                        )

                        else -> RoundedCornerShape(0.dp)
                    }
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

/**
 * Step 2: Amount input with auto-focus and validation
 */
@Composable
fun AmountInputStep(
    currentAmount: Money?,
    onAmountChanged: (Money) -> Unit,
    onNextClicked: () -> Unit
) {
    var amountInput by rememberSaveable { mutableStateOf(currentAmount?.asDouble()?.toString() ?: "") }
    val isValid = amountInput.toDoubleOrNull()?.let { it > 0 } == true
    val keyboardController = LocalSoftwareKeyboardController.current

    // Add focus requester for auto-focus
    val focusRequester = remember { FocusRequester() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Gib den Betrag ein (in Euro)",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        // Custom large number input, borderless, with euro symbol
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .width(280.dp)
                .padding(vertical = 16.dp)
        ) {
            BasicTextField(
                value = amountInput,
                onValueChange = {
                    // Only allow numbers and decimal separator
                    val newValue = it.replace(',', '.')
                        .filter { char -> char.isDigit() || char == '.' }
                    // Prevent more than one decimal point
                    if (newValue.count { c -> c == '.' } <= 1) {
                        amountInput = newValue
                        val value = newValue.toDoubleOrNull()
                        if (value != null && value > 0) onAmountChanged(Money(value))
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (isValid) {
                            keyboardController?.hide()
                            onNextClicked()
                        }
                    }
                ),
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = MaterialTheme.typography.displayLarge.fontSize,
                    fontWeight = MaterialTheme.typography.displayLarge.fontWeight,
                    textAlign = TextAlign.Center,
                    letterSpacing = MaterialTheme.typography.displayLarge.letterSpacing
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "€",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.displayLarge
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                keyboardController?.hide()
                onNextClicked()
            },
            enabled = isValid
        ) {
            Text("Weiter")
        }
    }

    // Auto-focus the text field when the composable is first displayed
    LaunchedEffect(Unit) {
        delay(100) // Small delay to ensure UI is ready
        focusRequester.requestFocus()
    }
}

/**
 * Step 3: Account selection with create account option
 */
@Composable
fun AccountSelectionStep(
    accounts: List<Account>,
    selectedAccount: Account?,
    onAccountSelected: (Account) -> Unit,
    onCreateAccountClicked: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Von welchem Konto?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (accounts.isEmpty()) {
            // No accounts available - show add account option prominently
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCreateAccountClicked() }
                    .clip(RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(20.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "➕ Neues Konto erstellen",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Es sind noch keine Konten vorhanden",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(accounts) { account ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAccountSelected(account) }
                            .clip(RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (account == selectedAccount)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                )
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = account.name,
                                color = if (account == selectedAccount)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Add new account option at the bottom
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCreateAccountClicked() }
                            .clip(RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "➕",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = "Neues Konto erstellen",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Step 4: ToAccount selection for transfers
 */
@Composable
fun ToAccountSelectionStep(
    accounts: List<Account>,
    selectedToAccount: Account?,
    onToAccountSelected: (Account) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Auf welches Konto soll transferiert werden?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (accounts.isEmpty()) {
            Text(
                text = "Kein verfügbares Zielkonto.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(accounts) { account ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onToAccountSelected(account) }
                            .clip(RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (account == selectedToAccount)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                )
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                text = account.name,
                                color = if (account == selectedToAccount)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Step 5: Partner input with smart suggestions
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PartnerInputStep(
    currentPartner: String,
    partnerSuggestions: List<String>,
    selectedDirection: TransactionDirection?,
    onPartnerChanged: (String) -> Unit,
    onNextClicked: () -> Unit
) {
    var input by rememberSaveable { mutableStateOf(currentPartner) }
    val isValid = input.trim().isNotEmpty()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Mit wem war die Transaktion?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = input,
            onValueChange = {
                input = it
                onPartnerChanged(it)
            },
            label = { Text("z.B. Netflix, Edeka, Max Mustermann") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (isValid) {
                        keyboardController?.hide()
                        onNextClicked()
                    }
                }
            ),
            modifier = Modifier.width(280.dp)
        )

        // Show suggestions based on transaction direction
        if (selectedDirection == TransactionDirection.Inflow) {
            // Zeige typische Einkommen/Partner und Nutzerhistorie für Einnahmen
            Spacer(Modifier.height(12.dp))
            val typicalInflowSources = if (partnerSuggestions.isNotEmpty()) {
                // Kombiniere Partner aus Nutzerhistorie und typischen Quellen
                (partnerSuggestions.take(5) + listOf(
                    "Arbeitgeber", "Freelance-Kunde", "Verkauf privat", "Steuererstattung", "Rückerstattung",
                    "Familie", "Freunde", "Zinsen Bank", "Dividenden"
                )).distinct().take(10)
            } else {
                // Nur typische Quellen, falls keine eigenen Partner
                listOf(
                    "Arbeitgeber", "Freelance-Kunde", "Verkauf privat", "Steuererstattung", "Rückerstattung",
                    "Familie", "Freunde", "Zinsen Bank", "Dividenden", "Cashback"
                )
            }

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                typicalInflowSources.forEach { source ->
                    SuggestionChip(
                        onClick = {
                            input = source
                            onPartnerChanged(source)
                        },
                        label = {
                            Text(text = source)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        } else if (partnerSuggestions.isNotEmpty()) {
            // Zeige tatsächliche, aus Ausgabenhistorie extrahierte Partner für Ausgaben/Übertragungen
            Spacer(Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                partnerSuggestions.take(10).forEach { suggestion ->
                    SuggestionChip(
                        onClick = {
                            input = suggestion
                            onPartnerChanged(suggestion)
                        },
                        label = {
                            Text(text = suggestion)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onNextClicked,
            enabled = isValid
        ) {
            Text("Weiter")
        }
    }
}

/**
 * Step 6: Category selection with AI suggestions
 */
@Composable
fun CategorySelectionStep(
    categories: List<Category>,
    selectedCategory: Category?,
    smartCategorizationState: SmartCategorizationUiState,
    onCategorySelected: (Category) -> Unit,
    onCreateCategoryClicked: () -> Unit,
    onAISuggestionAccepted: (Long) -> Unit,
    onNewAICategoryCreated: (NewCategorySuggestion) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Zu welcher Kategorie gehört das?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        // Show AI suggestion if available
        smartCategorizationState.categorizationResult?.let { result ->
            result.existingCategoryMatch?.let { match ->
                if (match.confidence > 0.5f) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clickable {
                                onAISuggestionAccepted(match.categoryId)
                            }
                            .clip(RoundedCornerShape(12.dp)),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "✨ KI-Vorschlag",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                    Text(
                                        text = "${match.categoryEmoji} ${match.categoryName}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    if (match.confidence > 0.8f) {
                                        Text(
                                            text = "Sehr sicher",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                                Button(
                                    onClick = { onAISuggestionAccepted(match.categoryId) },
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text("Verwenden")
                                }
                            }
                        }
                    }
                }
            }

            result.newCategorySuggestion?.let { suggestion ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clickable { onNewAICategoryCreated(suggestion) }
                        .clip(RoundedCornerShape(12.dp)),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "🤖 Neue Kategorie vorgeschlagen",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                                Text(
                                    text = "${suggestion.emoji} ${suggestion.categoryName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = suggestion.reasoning,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                )
                            }
                            Button(
                                onClick = { onNewAICategoryCreated(suggestion) },
                                modifier = Modifier.padding(start = 8.dp)
                            ) {
                                Text("Erstellen")
                            }
                        }
                    }
                }
            }
        }

        if (categories.isEmpty()) {
            // No categories available - show add category option prominently
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onCreateCategoryClicked() }
                    .clip(RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(20.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "➕ Neue Kategorie erstellen",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Es sind noch keine Kategorien vorhanden",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            // Categories available - show list with add option at bottom
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategorySelected(category) }
                            .clip(RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (category == selectedCategory)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                )
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = category.emoji,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = category.name,
                                    color = if (category == selectedCategory)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Add new category option at the bottom
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCreateCategoryClicked() }
                            .clip(RoundedCornerShape(12.dp)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "➕",
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = "Neue Kategorie erstellen",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Step 7: Date selection with localized formatting
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectionStep(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onNextClicked: () -> Unit
) {
    // Use rememberSaveable for dialog visibility, and the picked date (in millis)
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    // DatePicker uses milliseconds since epoch
    val initialMillis = selectedDate?.atStartOfDay(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        ?: Clock.System.now().toEpochMilliseconds()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis
    )

    // Update the currently shown date
    val pickedDateMillis = datePickerState.selectedDateMillis
    val pickedDate = pickedDateMillis?.let {
        java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
    }
    val isValid = pickedDate != null

    // Get current locale for formatting
    val locale = java.util.Locale.getDefault()
    val dateFormatter = remember(locale) { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Wann war die Transaktion?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Date display button - now a TextButton with locale-based formatting
        TextButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.width(240.dp)
        ) {
            val dateStr = pickedDate?.let { dateFormatter.format(it) } ?: "Datum wählen"
            Text(dateStr)
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                pickedDate?.let {
                    onDateSelected(it)
                    onNextClicked()
                }
            },
            enabled = isValid
        ) {
            Text("Weiter")
        }
    }

    // Show DatePickerDialog if toggled on
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pickedDate != null) {
                            onDateSelected(pickedDate)
                            showDatePicker = false
                        }
                    },
                    enabled = isValid
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

/**
 * Step 8: Optional description input
 */
@Composable
fun OptionalStep(
    description: String,
    onDescriptionChanged: (String) -> Unit,
    onFinishClicked: () -> Unit
) {
    var desc by rememberSaveable { mutableStateOf(description) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Möchtest du eine Notiz hinzufügen?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = desc,
            onValueChange = {
                desc = it
                onDescriptionChanged(it)
            },
            label = { Text("Notiz (optional)") },
            singleLine = false,
            maxLines = 3,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    onFinishClicked()
                }
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onFinishClicked,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("✨ Transaktion speichern")
        }
    }
}

/**
 * Step 9: Transaction completion celebration
 */
@Composable
fun TransactionCompletedStep(
    onSwitchToStandardMode: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = onSuccess,
            modifier = Modifier.size(80.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = "🎉 Geschafft!",
            style = MaterialTheme.typography.headlineMedium,
            color = onSuccess,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Deine erste Transaktion wurde erfolgreich gespeichert.\nAb sofort siehst du die Schnellansicht.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onSwitchToStandardMode,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Text("Weitere Transaktion hinzufügen")
        }
    }
}

/**
 * Checklist showing completed steps in guided mode
 * Performance optimized - no ViewModel injection needed
 */
@Composable
fun CompletedStepsChecklist(
    uiState: AddTransactionUiState,
    onStepClicked: (GuidedTransactionStep) -> Unit
) {
    val steps = GuidedTransactionStep.entries

    // Filter out ToAccount step if it's not a transfer
    val relevantSteps = steps.filter { step ->
        when (step) {
            GuidedTransactionStep.ToAccount -> uiState.selectedDirection == TransactionDirection.Unknown
            else -> true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        relevantSteps.take(relevantSteps.indexOf(uiState.guidedStep)).forEach { step ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onStepClicked(step) }
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${step.localizedName()}: ${getStepValue(step, uiState)}",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

