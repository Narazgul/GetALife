package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.RecurrenceFrequency
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.domain.model.categorization.NewCategorySuggestion
import app.tinygiants.getalife.presentation.main_app.transaction.composables.AddTransactionItem
import app.tinygiants.getalife.presentation.main_app.transaction.composables.Description
import app.tinygiants.getalife.presentation.main_app.transaction.composables.SmartCategorizationBottomSheet
import app.tinygiants.getalife.presentation.main_app.transaction.composables.TransactionPartner
import app.tinygiants.getalife.presentation.main_app.transaction.composables.waveAnimationBackground
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.onSuccess
import app.tinygiants.getalife.theme.spacing
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Clock

@Composable
fun AddTransaction() {
    val viewModel: AddTransactionViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isGuidedMode) {
        GuidedTransactionScreen(viewModel = viewModel, uiState = uiState)
    } else {
        StandardTransactionScreen(viewModel = viewModel, uiState = uiState)
    }
}

@Composable
fun GuidedTransactionScreen(viewModel: AddTransactionViewModel, uiState: AddTransactionUiState) {
    val progress = remember(uiState.guidedStep) {
        (GuidedTransactionStep.entries.indexOf(uiState.guidedStep) + 1).toFloat() / GuidedTransactionStep.entries.size.toFloat()
    }

    val neutralBackground = MaterialTheme.colorScheme.primary.toArgb()
    val inflowBackground = onSuccess.toArgb()
    val outflowBackground = MaterialTheme.colorScheme.errorContainer.toArgb()
    val transferBackground = MaterialTheme.colorScheme.tertiary.toArgb()

    val waveColor = when (uiState.selectedDirection) {
        TransactionDirection.Inflow -> inflowBackground
        TransactionDirection.Outflow -> outflowBackground
        TransactionDirection.Unknown -> transferBackground // Transfer
        else -> neutralBackground
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
        ) {
            // Animated Wave Background (matches StandardTransactionContent)
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(450.dp) // Increased to match StandardTransactionContent
                    .waveAnimationBackground(color = waveColor)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(spacing.m)
            ) {
                // Progress Header (overlaid on background)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            WindowInsets.statusBars.asPaddingValues()
                        )
                        .padding(bottom = spacing.s),
                    color = MaterialTheme.colorScheme.onPrimary
                )

                Text(
                    text = viewModel.getProgressText(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    modifier = Modifier
                        .padding(
                            start = WindowInsets.statusBars.asPaddingValues()
                                .calculateLeftPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                            end = WindowInsets.statusBars.asPaddingValues()
                                .calculateRightPadding(androidx.compose.ui.unit.LayoutDirection.Ltr)
                        )
                        .background(
                            color = Color.Black.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                    // Move bottom padding outside the background, to have only content (text) padded inside the background
                )
                Spacer(modifier = Modifier.padding(bottom = spacing.l))

                // Checklist of completed steps
                CompletedStepsChecklist(
                    uiState = uiState,
                    onStepClicked = { step -> viewModel.goToStep(step) }
                )

                // Focus Area - Current Step
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = uiState.guidedStep,
                        label = "Guided Step Animation",
                        transitionSpec = {
                            slideInVertically(animationSpec = tween(400)) { height -> height } +
                                    fadeIn(animationSpec = tween(400)) togetherWith
                                    slideOutVertically(animationSpec = tween(400)) { height -> -height } +
                                    fadeOut(animationSpec = tween(400))
                        }
                    ) { targetStep ->
                        when (targetStep) {
                            GuidedTransactionStep.Type -> TransactionTypeStep(
                                selectedDirection = uiState.selectedDirection,
                                onTypeSelected = viewModel::onGuidedTransactionTypeSelected
                            )

                            GuidedTransactionStep.Amount -> AmountInputStep(
                                currentAmount = uiState.selectedAmount,
                                onAmountChanged = viewModel::onGuidedAmountEntered,
                                onNextClicked = viewModel::moveToNextStep
                            )

                            GuidedTransactionStep.Account -> AccountSelectionStep(
                                accounts = uiState.accounts,
                                selectedAccount = uiState.selectedAccount,
                                onAccountSelected = viewModel::onGuidedAccountSelected
                            )

                            GuidedTransactionStep.ToAccount -> ToAccountSelectionStep(
                                accounts = uiState.accounts.filter { it != uiState.selectedAccount }, // Exclude source account
                                selectedToAccount = uiState.selectedToAccount,
                                onToAccountSelected = viewModel::onGuidedToAccountSelected
                            )

                            GuidedTransactionStep.Partner -> PartnerInputStep(
                                currentPartner = uiState.selectedPartner,
                                onPartnerChanged = viewModel::onGuidedPartnerEntered,
                                onNextClicked = viewModel::moveToNextStep
                            )

                            GuidedTransactionStep.Category -> CategorySelectionStep(
                                categories = uiState.categories,
                                selectedCategory = uiState.selectedCategory,
                                onCategorySelected = viewModel::onGuidedCategorySelected
                            )

                            GuidedTransactionStep.Date -> DateSelectionStep(
                                selectedDate = uiState.selectedDate,
                                onDateSelected = viewModel::onGuidedDateSelected,
                                onNextClicked = viewModel::moveToNextStep
                            )

                            GuidedTransactionStep.Optional -> OptionalStep(
                                description = uiState.selectedDescription,
                                onDescriptionChanged = viewModel::onGuidedDescriptionChanged,
                                onFinishClicked = viewModel::onGuidedTransactionComplete
                            )

                            GuidedTransactionStep.Done -> TransactionCompletedStep()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StandardTransactionScreen(viewModel: AddTransactionViewModel, uiState: AddTransactionUiState) {
    val partnerSuggestions by viewModel.partners.collectAsStateWithLifecycle()
    val smartCategorizationState by viewModel.smartCategorizationState.collectAsStateWithLifecycle()
    val transactionPartner by viewModel.transactionPartner.collectAsStateWithLifecycle()

    val onAddTransactionClicked =
        { amount: Money,
          account: Account,
          category: Category?,
          direction: TransactionDirection,
          description: Description,
          transactionPartner: TransactionPartner,
          dateOfTransaction: kotlin.time.Instant,
          recurrenceFrequency: RecurrenceFrequency? ->

            viewModel.onSaveTransactionClicked(
                amount = amount,
                direction = direction,
                accountId = account.id,
                description = description,
                transactionPartner = transactionPartner,
                category = category,
                dateOfTransaction = dateOfTransaction,
                recurrenceFrequency = recurrenceFrequency
            )
        }

    StandardTransactionContent(
        categories = uiState.categories,
        accounts = uiState.accounts,
        partnerSuggestions = partnerSuggestions,
        selectedCategory = uiState.selectedCategory,
        transactionPartner = transactionPartner,
        onAddTransactionClicked = onAddTransactionClicked,
        onTransactionPartnerChanged = viewModel::updateTransactionPartner,
        onTransactionDescriptionChanged = viewModel::updateTransactionDescription,
        onTransactionAmountChanged = viewModel::updateTransactionAmount,
        smartCategorizationState = smartCategorizationState,
        onCategorySuggestionAccepted = { categoryId -> viewModel.onCategorySuggestionAccepted(categoryId) },
        onNewCategoryCreated = viewModel::onCreateCategoryFromSuggestion,
        onSmartCategorizationDismissed = viewModel::dismissCategorizationBottomSheet
    )
}

@Composable
fun StandardTransactionContent(
    categories: List<Category>,
    accounts: List<Account>,
    partnerSuggestions: List<String>,
    selectedCategory: Category?,
    transactionPartner: String,
    onAddTransactionClicked: (amount: Money, account: Account, category: Category?, direction: TransactionDirection, description: Description, transactionPartner: TransactionPartner, dateOfTransaction: kotlin.time.Instant, recurrenceFrequency: RecurrenceFrequency?) -> Unit,
    onTransactionPartnerChanged: (String) -> Unit,
    onTransactionDescriptionChanged: (String) -> Unit,
    onTransactionAmountChanged: (Money) -> Unit,
    smartCategorizationState: SmartCategorizationUiState,
    onCategorySuggestionAccepted: (Long) -> Unit,
    onNewCategoryCreated: (NewCategorySuggestion) -> Unit,
    onSmartCategorizationDismissed: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val neutralBackground = MaterialTheme.colorScheme.primary.toArgb()
    val inflowBackground = onSuccess.toArgb()
    val outflowBackground = MaterialTheme.colorScheme.errorContainer.toArgb()
    val transferBackground = MaterialTheme.colorScheme.tertiary.toArgb()
    var waveColor by remember { mutableIntStateOf(neutralBackground) }

    val transactionSavedString = stringResource(id = R.string.transaction_saved)
    val showTransactionAddedSnackbar = { scope.launch { snackbarHostState.showSnackbar(transactionSavedString) } }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(450.dp)
                    .waveAnimationBackground(color = waveColor)
            )

            AddTransactionItem(
                categories = categories,
                accounts = accounts,
                partnerSuggestions = partnerSuggestions,
                selectedCategory = selectedCategory,
                transactionPartner = transactionPartner,
                smartCategorizationUiState = smartCategorizationState,
                onTransactionPartnerChanged = onTransactionPartnerChanged,
                onTransactionDirectionClicked = { transactionDirection ->
                    waveColor = when (transactionDirection) {
                        TransactionDirection.Inflow -> inflowBackground
                        TransactionDirection.Outflow -> outflowBackground
                        TransactionDirection.Unknown -> transferBackground // Transfer
                        else -> neutralBackground
                    }
                },
                onAddTransactionClicked = { amount, account, category, direction, description, transactionPartner, dateOfTransaction, recurrenceFrequency ->
                    onAddTransactionClicked(
                        amount,
                        account,
                        category,
                        direction,
                        description,
                        transactionPartner,
                        dateOfTransaction,
                        recurrenceFrequency
                    )
                    showTransactionAddedSnackbar()
                },
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(spacing.m)
            )

            // Smart Categorization Bottom Sheet
            smartCategorizationState.categorizationResult?.let { result ->
                SmartCategorizationBottomSheet(
                    categorizationResult = result,
                    isVisible = smartCategorizationState.showBottomSheet,
                    onDismiss = onSmartCategorizationDismissed,
                    onCategorySelected = onCategorySuggestionAccepted,
                    onNewCategoryCreated = onNewCategoryCreated
                )
            }
        }
    }
}

// ================================
// Guided Transaction UI Components
// ================================

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
                    .then(
                        Modifier
                            .pointerHoverIcon(icon = PointerIcon.Hand)
                    )
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
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = "Bearbeiten",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(16.dp)
                        .padding(start = 6.dp)
                )
            }
        }
    }
}

fun getStepValue(step: GuidedTransactionStep, uiState: AddTransactionUiState): String {
    return when (step) {
        GuidedTransactionStep.Type -> when (uiState.selectedDirection) {
            TransactionDirection.Inflow -> "Einnahme"
            TransactionDirection.Outflow -> "Ausgabe"
            else -> "Transfer"
        }

        GuidedTransactionStep.Amount -> uiState.selectedAmount?.formattedMoney ?: ""
        GuidedTransactionStep.Account -> uiState.selectedAccount?.name ?: ""
        GuidedTransactionStep.ToAccount -> uiState.selectedToAccount?.name ?: ""
        GuidedTransactionStep.Partner -> uiState.selectedPartner
        GuidedTransactionStep.Category -> uiState.selectedCategory?.name ?: ""
        GuidedTransactionStep.Date -> uiState.selectedDate?.toString() ?: ""
        GuidedTransactionStep.Optional -> if (uiState.selectedDescription.isNotEmpty()) "Beschreibung hinzugefügt" else "Übersprungen"
        GuidedTransactionStep.Done -> "Fertig"
    }
}

fun GuidedTransactionStep.localizedName(): String = when (this) {
    GuidedTransactionStep.Type -> "Typ"
    GuidedTransactionStep.Amount -> "Betrag"
    GuidedTransactionStep.Account -> "Konto"
    GuidedTransactionStep.ToAccount -> "Zielkonto"
    GuidedTransactionStep.Partner -> "Partner"
    GuidedTransactionStep.Category -> "Kategorie"
    GuidedTransactionStep.Date -> "Datum"
    GuidedTransactionStep.Optional -> "Optionen"
    GuidedTransactionStep.Done -> "Fertig"
}

@Composable
fun TransactionTypeStep(
    selectedDirection: TransactionDirection?,
    onTypeSelected: (TransactionDirection) -> Unit
) {
    val viewModel: AddTransactionViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
            if (uiState.accounts.size >= 2) {
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
                cursorBrush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary),
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
    androidx.compose.runtime.LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(100) // Small delay to ensure UI is ready
        focusRequester.requestFocus()
    }
}

@Composable
fun AccountSelectionStep(
    accounts: List<Account>,
    selectedAccount: Account?,
    onAccountSelected: (Account) -> Unit
) {
    val viewModel: AddTransactionViewModel = hiltViewModel()
    var showAddAccountDialog by rememberSaveable { mutableStateOf(false) }

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
                    .clickable { showAddAccountDialog = true }
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
                            .clickable { showAddAccountDialog = true }
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

        // Add Account Dialog
        if (showAddAccountDialog) {
            AddAccountDialog(
                onDismiss = { showAddAccountDialog = false },
                onAccountCreated = { accountName, initialBalance, accountType ->
                    viewModel.onCreateNewAccount(accountName, initialBalance, accountType)
                    showAddAccountDialog = false
                }
            )
        }
    }
}

/**
 * Step UI for selecting the ToAccount in Transfers.
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PartnerInputStep(
    currentPartner: String,
    onPartnerChanged: (String) -> Unit,
    onNextClicked: () -> Unit
) {
    var input by rememberSaveable { mutableStateOf(currentPartner) }
    val isValid = input.trim().isNotEmpty()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Use ViewModel to get partnerSuggestions
    val viewModel: AddTransactionViewModel = hiltViewModel()
    val partnerSuggestions by viewModel.partners.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
        if (uiState.selectedDirection == TransactionDirection.Inflow) {
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

@Composable
fun CategorySelectionStep(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit
) {
    val viewModel: AddTransactionViewModel = hiltViewModel()
    val smartCategorizationState by viewModel.smartCategorizationState.collectAsStateWithLifecycle()
    var showAddCategoryDialog by rememberSaveable { mutableStateOf(false) }

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
                                categories.find { it.id == match.categoryId }?.let { category ->
                                    onCategorySelected(category)
                                }
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
                                    onClick = {
                                        categories.find { it.id == match.categoryId }?.let { category ->
                                            onCategorySelected(category)
                                        }
                                    },
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
                        .clickable {
                            val newCategory = Category(
                                id = System.currentTimeMillis(),
                                groupId = suggestion.groupId,
                                emoji = suggestion.emoji,
                                name = suggestion.categoryName,
                                budgetTarget = suggestion.suggestedBudget ?: Money(0.0),
                                monthlyTargetAmount = null,
                                targetMonthsRemaining = null,
                                listPosition = 999,
                                isInitialCategory = false,
                                linkedAccountId = null,
                                updatedAt = Clock.System.now(),
                                createdAt = Clock.System.now()
                            )
                            onCategorySelected(newCategory)
                        }
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
                                onClick = {
                                    val newCategory = Category(
                                        id = System.currentTimeMillis(),
                                        groupId = suggestion.groupId,
                                        emoji = suggestion.emoji,
                                        name = suggestion.categoryName,
                                        budgetTarget = suggestion.suggestedBudget ?: Money(0.0),
                                        monthlyTargetAmount = null,
                                        targetMonthsRemaining = null,
                                        listPosition = 999,
                                        isInitialCategory = false,
                                        linkedAccountId = null,
                                        updatedAt = Clock.System.now(),
                                        createdAt = Clock.System.now()
                                    )
                                    onCategorySelected(newCategory)
                                },
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
                    .clickable { showAddCategoryDialog = true }
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
                            .clickable { showAddCategoryDialog = true }
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

        // Add Category Dialog
        if (showAddCategoryDialog) {
            AddCategoryDialog(
                onDismiss = { showAddCategoryDialog = false },
                onCategoryCreated = { categoryName ->
                    viewModel.onCreateNewCategory(categoryName)
                    showAddCategoryDialog = false
                }
            )
        }
    }
}

/**
 * Dialog for creating a new category.
 * Allows user to input a name, emoji will be automatically generated by AI.
 */
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

/**
 * Dialog for creating a new account.
 * Allows user to input name, starting balance, and account type.
 */
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
    val balance = balanceInput.replace(',', '.').toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Neues Konto erstellen", style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Account Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Kontoname") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )

                // Starting Balance Input
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

                // Account Type Selection
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
                        onValueChange = { /* Read-only */ },
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

@Composable
fun TransactionCompletedStep() {
    val viewModel: AddTransactionViewModel = hiltViewModel()

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
            onClick = { viewModel.switchToStandardMode() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Text("Weitere Transaktion hinzufügen")
        }
    }
}

// ================================
// Preview Composables
// ================================

@Preview(name = "Guided - Type Selection")
@Composable
private fun GuidedTypePreview() {
    GetALifeTheme {
        Surface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Was für eine Transaktion ist das?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )

                val options = listOf(
                    TransactionDirection.Inflow to "💰 Einnahme",
                    TransactionDirection.Unknown to "↔️ Transfer",
                    TransactionDirection.Outflow to "💸 Ausgabe"
                )

                MultiChoiceSegmentedButtonRow {
                    options.forEachIndexed { idx, (direction, label) ->
                        SegmentedButton(
                            checked = false,
                            onCheckedChange = { },
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
    }
}

@Preview(name = "Guided - Type Selection with Transfer")
@Composable
private fun GuidedTypeWithTransferPreview() {
    GetALifeTheme {
        Surface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Was für eine Transaktion ist das?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )

                val options = listOf(
                    TransactionDirection.Inflow to "💰 Einnahme",
                    TransactionDirection.Unknown to "↔️ Transfer",
                    TransactionDirection.Outflow to "💸 Ausgabe"
                )

                MultiChoiceSegmentedButtonRow {
                    options.forEachIndexed { idx, (direction, label) ->
                        SegmentedButton(
                            checked = direction == TransactionDirection.Inflow,
                            onCheckedChange = { },
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
    }
}

@Preview(name = "Guided - Amount Input")
@Composable
private fun GuidedAmountPreview() {
    GetALifeTheme {
        Surface {
            AmountInputStep(
                currentAmount = Money(25.50),
                onAmountChanged = { },
                onNextClicked = { }
            )
        }
    }
}

@Preview(name = "Guided - Account Selection")
@Composable
private fun GuidedAccountPreview() {
    GetALifeTheme {
        Surface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Von welchem Konto?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val accounts = listOf(
                        Account(
                            id = 1L,
                            name = "Girokonto",
                            type = AccountType.Checking,
                            balance = Money(1250.0),
                            listPosition = 0,
                            createdAt = Clock.System.now(),
                            updatedAt = Clock.System.now()
                        ),
                        Account(
                            id = 2L,
                            name = "Sparkonto",
                            type = AccountType.Savings,
                            balance = Money(5000.0),
                            listPosition = 1,
                            createdAt = Clock.System.now(),
                            updatedAt = Clock.System.now()
                        )
                    )

                    items(accounts) { account ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(16.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = account.name,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // Add new account option at the bottom
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
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
}

@Preview(name = "Guided - Category Selection with Progress")
@Composable
private fun GuidedCategoryWithProgressPreview() {
    GetALifeTheme {
        Surface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Zu welcher Kategorie gehört das?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )

                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf(
                        Category(
                            id = 1L,
                            groupId = 1L,
                            emoji = "🍕",
                            name = "Lebensmittel",
                            budgetTarget = Money(100.0),
                            monthlyTargetAmount = null,
                            targetMonthsRemaining = null,
                            listPosition = 1,
                            isInitialCategory = false,
                            linkedAccountId = null,
                            updatedAt = Clock.System.now(),
                            createdAt = Clock.System.now()
                        ),
                        Category(
                            id = 2L,
                            groupId = 1L,
                            emoji = "🏠",
                            name = "Miete",
                            budgetTarget = Money(800.0),
                            monthlyTargetAmount = null,
                            targetMonthsRemaining = null,
                            listPosition = 2,
                            isInitialCategory = false,
                            linkedAccountId = null,
                            updatedAt = Clock.System.now(),
                            createdAt = Clock.System.now()
                        )
                    )

                    items(categories) { category ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surface)
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
                                        color = MaterialTheme.colorScheme.onSurface
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
}

@Preview(name = "Guided - Transfer ToAccount")
@Composable
private fun GuidedTransferPreview() {
    GetALifeTheme {
        Surface {
            ToAccountSelectionStep(
                accounts = listOf(
                    Account(
                        id = 2L,
                        name = "Sparkonto",
                        type = AccountType.Savings,
                        balance = Money(5000.0),
                        listPosition = 0,
                        createdAt = Clock.System.now(),
                        updatedAt = Clock.System.now()
                    ),
                    Account(
                        id = 3L,
                        name = "Depot",
                        type = AccountType.Depot,
                        balance = Money(15000.0),
                        listPosition = 1,
                        createdAt = Clock.System.now(),
                        updatedAt = Clock.System.now()
                    )
                ),
                selectedToAccount = null,
                onToAccountSelected = { }
            )
        }
    }
}

@Preview(name = "Guided - Partner Input")
@Composable
private fun GuidedPartnerPreview() {
    GetALifeTheme {
        Surface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Mit wem war die Transaktion?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    label = { Text("z.B. Netflix, Edeka, Max Mustermann") },
                    singleLine = true,
                    modifier = Modifier.width(280.dp)
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = { },
                    enabled = false
                ) {
                    Text("Weiter")
                }
            }
        }
    }
}

@Preview(name = "Guided - Date Selection")
@Composable
private fun GuidedDatePreview() {
    GetALifeTheme {
        Surface {
            DateSelectionStep(
                selectedDate = LocalDate.now(),
                onDateSelected = { },
                onNextClicked = { }
            )
        }
    }
}

@Preview(name = "Guided - Optional Step")
@Composable
private fun GuidedOptionalPreview() {
    GetALifeTheme {
        Surface {
            OptionalStep(
                description = "",
                onDescriptionChanged = { },
                onFinishClicked = { }
            )
        }
    }
}

@Preview(name = "Guided - Empty Categories")
@Composable
private fun GuidedEmptyCategoryPreview() {
    GetALifeTheme {
        Surface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Zu welcher Kategorie gehört das?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )

                // No categories available - show add category option prominently
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
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
            }
        }
    }
}

@Preview(name = "Guided - Empty Accounts")
@Composable
private fun GuidedEmptyAccountPreview() {
    GetALifeTheme {
        Surface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Von welchem Konto?",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // No accounts available - show add account option prominently
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
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
            }
        }
    }
}

@Preview(name = "Guided - Completed")
@Composable
private fun GuidedCompletedPreview() {
    GetALifeTheme {
        Surface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
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
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    Text("Weitere Transaktion hinzufügen")
                }
            }
        }
    }
}

@Preview(name = "Standard Transaction Screen")
@Composable
private fun StandardTransactionPreview() {
    GetALifeTheme {
        Surface {
            StandardTransactionContent(
                categories = emptyList(),
                accounts = emptyList(),
                partnerSuggestions = emptyList(),
                selectedCategory = null,
                transactionPartner = "",
                onAddTransactionClicked = { _, _, _, _, _, _, _, _ -> },
                onTransactionPartnerChanged = { },
                onTransactionDescriptionChanged = { },
                onTransactionAmountChanged = { },
                smartCategorizationState = SmartCategorizationUiState(),
                onCategorySuggestionAccepted = { },
                onNewCategoryCreated = { },
                onSmartCategorizationDismissed = {}
            )
        }
    }
}