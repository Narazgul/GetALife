package app.tinygiants.getalife.presentation.main_app.transaction.transactions.composables

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Transaction
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.theme.spacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Clock
import kotlin.time.Instant
import app.tinygiants.getalife.presentation.shared_composables.InputValidationUtils

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
        description: String,
        transactionPartner: String,
        dateOfTransaction: Instant
    ) -> Unit,
    onDeleteTransactionClicked: () -> Unit = {},
) {

    var showCategoryDropdown by rememberSaveable { mutableStateOf(false) }
    var showAccountDropdown by rememberSaveable { mutableStateOf(false) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    var amountMoney by remember {
        mutableStateOf(transaction?.amount?.let { Money(kotlin.math.abs(it.asDouble())) } ?: Money(value = 0.0))
    }
    var amountUserInputText by rememberSaveable { mutableStateOf("") }
    var descriptionUserInput by rememberSaveable { mutableStateOf(transaction?.description ?: "") }
    var transactionPartnerUserInput by rememberSaveable { mutableStateOf(transaction?.transactionPartner ?: "") }
    var transactionDirectionUserInput by rememberSaveable {
        mutableStateOf(transaction?.transactionDirection ?: TransactionDirection.Unknown)
    }
    var categoryUserInput by remember { mutableStateOf(transaction?.category) }
    var accountUserInput by remember { mutableStateOf(transaction?.account) }
    var selectedDate by remember { mutableStateOf(transaction?.dateOfTransaction ?: Clock.System.now()) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochMilliseconds()
    )
    val confirmEnabled = remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

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
                text = "Transaktion bearbeiten",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = spacing.l)
            )

            // Direction Selection - keeping the existing beautiful design
            MultiChoiceSegmentedButtonRow {
                SegmentedButton(
                    checked = transactionDirectionUserInput == TransactionDirection.Inflow,
                    onCheckedChange = { isChecked ->
                        transactionDirectionUserInput = if (isChecked) TransactionDirection.Inflow
                        else TransactionDirection.Outflow
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
                    },
                    shape = RoundedCornerShape(topEnd = spacing.l, bottomEnd = spacing.l),
                ) {
                    Text(text = stringResource(R.string.outflow))
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
                        visible = transactionDirectionUserInput != TransactionDirection.Inflow,
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
                        selectedText = formatTransactionDate(selectedDate),
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
                                val parsedAmount =
                                    InputValidationUtils.parseAmountInput(amountUserInputText, Money(amountMoney.asDouble()))
                                amountMoney = parsedAmount
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

            // Transaction Partner Input - Full Width
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(spacing.l)
            ) {
                TextField(
                    value = transactionPartnerUserInput,
                    onValueChange = { userInput -> transactionPartnerUserInput = userInput },
                    label = {
                        Text(
                            stringResource(R.string.transaction_partner),
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(spacing.m)
                )
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

            Spacer(modifier = Modifier.height(spacing.xl))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing.m)
            ) {
                // Delete Button (if editing existing transaction)
                if (transaction != null) {
                    Button(
                        onClick = {
                            onDeleteTransactionClicked()
                            onDismissRequest()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(spacing.l),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.delete_transaction),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }

                // Save Button
                Button(
                    onClick = {
                        onSaveTransactionClicked(
                            amountMoney,
                            accountUserInput!!,
                            categoryUserInput,
                            transactionDirectionUserInput,
                            descriptionUserInput,
                            transactionPartnerUserInput,
                            selectedDate
                        )
                        onDismissRequest()
                    },
                    enabled = accountUserInput != null &&
                            transactionDirectionUserInput != TransactionDirection.Unknown &&
                            (categoryUserInput != null || transactionDirectionUserInput == TransactionDirection.Inflow),
                    modifier = Modifier
                        .weight(if (transaction != null) 1f else 2f)
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

            // Bottom padding for better spacing and navigation bar
            Spacer(modifier = Modifier.height(48.dp))
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

private fun formatTransactionDate(instant: Instant): String {
    val date = Date(instant.toEpochMilliseconds())
    return SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN).format(date)
}