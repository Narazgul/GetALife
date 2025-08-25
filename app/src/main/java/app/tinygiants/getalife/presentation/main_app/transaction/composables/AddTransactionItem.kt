package app.tinygiants.getalife.presentation.main_app.transaction.composables

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.RecurrenceFrequency
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.model.asStringRes
import app.tinygiants.getalife.domain.model.categorization.NewCategorySuggestion
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.SmartCategorizationUiState
import app.tinygiants.getalife.presentation.shared_composables.AutoCompleteTextField
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.Instant

typealias Description = String
typealias TransactionPartner = String

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionItem(
    categories: List<Category>,
    accounts: List<Account>,
    partnerSuggestions: List<String> = emptyList(),
    selectedCategory: Category? = null,
    transactionPartner: String = "",
    smartCategorizationUiState: SmartCategorizationUiState = SmartCategorizationUiState(),
    onTransactionPartnerChanged: (String) -> Unit = {},
    onAcceptCategorySuggestion: (NewCategorySuggestion) -> Unit = {},
    onTransactionDirectionClicked: (TransactionDirection) -> Unit,
    onAddTransactionClicked: (
        amount: Money,
        account: Account,
        category: Category?,
        direction: TransactionDirection,
        description: Description,
        transactionPartner: TransactionPartner,
        dateOfTransaction: Instant,
        recurrenceFrequency: RecurrenceFrequency?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    var showCategoryDropdown by rememberSaveable { mutableStateOf(false) }
    var showAccountDropdown by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    var amountMoney by remember { mutableStateOf(Money(value = 0.0)) }
    var amountUserInputText by rememberSaveable { mutableStateOf("") }
    var descriptionUserInput by rememberSaveable { mutableStateOf("") }
    var transactionPartnerUserInput by rememberSaveable { mutableStateOf("") }
    var directionUserInput by rememberSaveable { mutableStateOf(TransactionDirection.Unknown) }
    var categoryUserInput by remember { mutableStateOf(selectedCategory ?: categories.firstOrNull()) }
    var accountUserInput by remember { mutableStateOf(accounts.firstOrNull()) }
    var selectedDate by remember { mutableStateOf(Clock.System.now()) }
    var recurrenceFrequency by rememberSaveable { mutableStateOf(RecurrenceFrequency.NEVER) }
    var showRecurrenceDropdown by rememberSaveable { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochMilliseconds()
    )
    val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        MultiChoiceSegmentedButtonRow {
            SegmentedButton(
                checked = directionUserInput == TransactionDirection.Inflow,
                onCheckedChange = { isChecked ->
                    directionUserInput = if (isChecked) TransactionDirection.Inflow
                    else TransactionDirection.Outflow

                    onTransactionDirectionClicked(directionUserInput)
                },
                shape = RoundedCornerShape(topStart = spacing.l, bottomStart = spacing.l),
            ) {
                Text(text = stringResource(R.string.inflow))
            }
            SegmentedButton(
                checked = directionUserInput == TransactionDirection.Outflow,
                onCheckedChange = { isChecked ->
                    directionUserInput = if (isChecked) TransactionDirection.Outflow
                    else TransactionDirection.Inflow

                    onTransactionDirectionClicked(directionUserInput)
                },
                shape = RoundedCornerShape(topEnd = spacing.l, bottomEnd = spacing.l),
            ) {
                Text(text = stringResource(R.string.outflow))
            }
        }

        Spacer(modifier = Modifier.height(spacing.l))

        AutoCompleteTextField(
            value = transactionPartner,
            onValueChange = onTransactionPartnerChanged,
            suggestions = partnerSuggestions,
            label = stringResource(R.string.transaction_partner),
            modifier = Modifier.fillMaxWidth()
        )

        // Smart Categorization Suggestion
        AnimatedVisibility(
            visible = smartCategorizationUiState.hasValidSuggestion
        ) {
            smartCategorizationUiState.categorizationResult?.let { result ->
                // Show existing category match
                result.existingCategoryMatch?.let { match ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = spacing.xs),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(spacing.m)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(spacing.m)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${match.categoryEmoji} ${match.categoryName}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    // Set the suggested category as selected
                                    categoryUserInput = categories.find { it.id == match.categoryId }
                                },
                                shape = RoundedCornerShape(spacing.s)
                            ) {
                                Text(
                                    text = "Verwenden",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.l))

        // 2x2 Grid for Category, Account, Date, Amount
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.m)
            ) {
                // Category (only visible for Outflow)
                AnimatedVisibility(
                    visible = directionUserInput != TransactionDirection.Inflow,
                    modifier = Modifier.weight(1f)
                ) {
                    Box {
                        SelectionCard(
                            label = stringResource(id = R.string.chose_category),
                            selectedText = categoryUserInput?.name ?: stringResource(id = R.string.chose_category),
                            onClick = { showCategoryDropdown = true },
                            isCompact = true
                        )
                        DropdownMenu(
                            expanded = showCategoryDropdown,
                            onDismissRequest = { showCategoryDropdown = false },
                            modifier = Modifier.width(200.dp)
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
                }

                // Account Selection
                Box(modifier = Modifier.weight(1f)) {
                    SelectionCard(
                        label = stringResource(id = R.string.choose_account),
                        selectedText = accountUserInput?.name ?: stringResource(id = R.string.choose_account),
                        onClick = { showAccountDropdown = true },
                        isCompact = true
                    )
                    DropdownMenu(
                        expanded = showAccountDropdown,
                        onDismissRequest = { showAccountDropdown = false },
                        modifier = Modifier.width(200.dp)
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
            }

            Spacer(modifier = Modifier.height(spacing.m))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.m)
            ) {
                // Date Selection
                SelectionCard(
                    label = "Datum",
                    selectedText = formatDate(selectedDate),
                    onClick = { showDatePicker = true },
                    icon = Icons.Default.DateRange,
                    isCompact = true,
                    modifier = Modifier.weight(1f)
                )

                // Amount Input
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(spacing.l)
                ) {
                    TextField(
                        value = amountUserInputText,
                        onValueChange = { userInput ->
                            amountUserInputText = userInput.replace(oldChar = ',', newChar = '.')
                            amountMoney = Money(value = amountUserInputText.toDoubleOrNull() ?: amountMoney.asDouble())
                        },
                        label = {
                            Text(
                                stringResource(R.string.amount),
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .onFocusChanged {
                                amountUserInputText = if (it.isFocused.not()) amountMoney.formattedMoney
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
        }
        Spacer(modifier = Modifier.height(spacing.l))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(spacing.l)
        ) {
            TextField(
                value = descriptionUserInput,
                onValueChange = { userInput -> descriptionUserInput = userInput },
                label = {
                    Text(
                        stringResource(R.string.description),
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyLarge,
                maxLines = 3,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(spacing.m)
            )
        }

        Spacer(modifier = Modifier.height(spacing.l))

        // Recurrence frequency selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(spacing.l)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showRecurrenceDropdown = true }
                    .padding(spacing.l)
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.recurrence_frequency_label),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Spacer(modifier = Modifier.height(spacing.xs))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(recurrenceFrequency.asStringRes()),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            DropdownMenu(
                expanded = showRecurrenceDropdown,
                onDismissRequest = { showRecurrenceDropdown = false },
                modifier = Modifier.width(250.dp)
            ) {
                RecurrenceFrequency.entries.forEach { frequency ->
                    DropdownMenuItem(
                        text = { Text(stringResource(frequency.asStringRes())) },
                        onClick = {
                            recurrenceFrequency = frequency
                            showRecurrenceDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(spacing.xl))

        Button(
            onClick = {
                onAddTransactionClicked(
                    amountMoney,
                    accountUserInput!!,
                    categoryUserInput,
                    directionUserInput,
                    descriptionUserInput,
                    transactionPartner,
                    selectedDate,
                    if (recurrenceFrequency != RecurrenceFrequency.NEVER) recurrenceFrequency else null
                )

                amountMoney = Money(value = 0.0)
                amountUserInputText = ""
                descriptionUserInput = ""
                directionUserInput = TransactionDirection.Unknown
                categoryUserInput = selectedCategory ?: categories.firstOrNull()
                accountUserInput = accounts.firstOrNull()
                selectedDate = Clock.System.now()
                recurrenceFrequency = RecurrenceFrequency.NEVER

                focusManager.clearFocus()
            },
            enabled = accountUserInput != null &&
                    directionUserInput != TransactionDirection.Unknown &&
                    (categoryUserInput != null || directionUserInput == TransactionDirection.Inflow),
            modifier = Modifier
                .fillMaxWidth(0.8f) // reduced width of the save button
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
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = Instant.fromEpochMilliseconds(millis)
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

@Composable
private fun SelectionCard(
    label: String,
    selectedText: String,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
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
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(if (isCompact) 16.dp else 20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
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
}

private fun formatDate(instant: Instant): String {
    val localDate = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
    return "${localDate.day.toString().padStart(2, '0')}.${
        localDate.month.number.toString().padStart(2, '0')
    }.${localDate.year}"
}

@Preview
@Composable
private fun EnterTransactionPreview() {
    GetALifeTheme {
        Surface {
            AddTransactionItem(
                categories = emptyList(),
                accounts = emptyList(),
                partnerSuggestions = emptyList(),
                transactionPartner = "",
                onTransactionDirectionClicked = { _ -> },
                onAddTransactionClicked = { _, _, _, _, _, _, _, _ -> })
        }
    }
}