package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.composables.AddAccountDialog
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.composables.AddCategoryDialog
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.AccountSelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.AmountInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.CategorySelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.CompletionStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.DateSelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.TextInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.TransactionTypeSelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.shared.CompletedStepsChecklist
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.shared.GuidedStepCounter
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.shared.TransactionAccountSelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.shared.TransactionAmountInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.shared.TransactionCategorySelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.shared.TransactionDateSelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.shared.TransactionDescriptionInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.shared.TransactionFlowSelector
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.shared.TransactionPartnerInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.shared.TransactionStepContainer
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.shared.getStepTitle
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing

@Composable
fun AddTransactionScreen() {
    // Single ViewModel injection point
    val viewModel: AddTransactionViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Dialog state management
    var showAddAccountDialog by rememberSaveable { mutableStateOf(false) }
    var showAddCategoryDialog by rememberSaveable { mutableStateOf(false) }

    TransactionStepContainer(
        transactionInput = uiState.transactionInput,
        currentStep = uiState.currentStep,
        isGuidedMode = uiState.isGuidedMode,
        showProgress = uiState.isGuidedMode // Only show progress in guided mode
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Main content takes most of the space
            Box(
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.isGuidedMode) {
                    GuidedStepContent(
                        currentStep = uiState.currentStep,
                        transactionInput = uiState.transactionInput,
                        categories = uiState.categories,
                        accounts = uiState.accounts,
                        viewModel = viewModel,
                        onShowAddAccountDialog = { showAddAccountDialog = true },
                        onShowAddCategoryDialog = { showAddCategoryDialog = true }
                    )
                } else {
                    StandardFormContent(
                        transactionInput = uiState.transactionInput,
                        categories = uiState.categories,
                        accounts = uiState.accounts,
                        viewModel = viewModel,
                        onShowAddAccountDialog = { showAddAccountDialog = true },
                        onShowAddCategoryDialog = { showAddCategoryDialog = true }
                    )
                }
            }

            // Debug Mode Switch Button
            Button(
                onClick = {
                    if (uiState.isGuidedMode) {
                        viewModel.switchToStandardMode()
                    } else {
                        viewModel.switchToGuidedMode()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(spacing.m),
                colors = ButtonDefaults.outlinedButtonColors(),
                shape = RoundedCornerShape(spacing.s)
            ) {
                Text(
                    text = if (uiState.isGuidedMode) {
                        "🔧 DEBUG: Switch to Standard Mode"
                    } else {
                        "🔧 DEBUG: Switch to Guided Mode"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    // Dialogs
    if (showAddAccountDialog) {
        AddAccountDialog(
            onDismiss = { showAddAccountDialog = false },
            onAccountCreated = { name, balance, type ->
                viewModel.onAccountCreated(name, balance, type)
                showAddAccountDialog = false
            }
        )
    }

    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onCategoryCreated = { name ->
                viewModel.onCategoryCreated(name)
                showAddCategoryDialog = false
            }
        )
    }
}

/**
 * Guided Mode: Step-by-step progression with animations
 */
@Composable
private fun GuidedStepContent(
    currentStep: TransactionStep,
    transactionInput: TransactionInput,
    categories: List<app.tinygiants.getalife.domain.model.Category>,
    accounts: List<app.tinygiants.getalife.domain.model.Account>,
    viewModel: AddTransactionViewModel,
    onShowAddAccountDialog: () -> Unit,
    onShowAddCategoryDialog: () -> Unit
) {
    // Local state for UI interactions (like in the original implementation)
    var amountText by rememberSaveable {
        mutableStateOf(transactionInput.amount?.asDouble()?.toString() ?: "")
    }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacing.m)
    ) {
        CompletedStepsChecklist(
            transactionInput = transactionInput,
            currentStep = currentStep,
            onStepClicked = viewModel::goToStep
        )

        GuidedStepCounter(
            currentStep = currentStep,
            transactionInput = transactionInput
        )

        AnimatedContent(
            targetState = currentStep,
            label = "Guided Step Animation",
            transitionSpec = {
                slideInHorizontally(
                    animationSpec = tween(300),
                    initialOffsetX = { fullWidth -> fullWidth }
                ) + fadeIn(animationSpec = tween(300)) togetherWith
                        slideOutHorizontally(
                            animationSpec = tween(300),
                            targetOffsetX = { fullWidth -> -fullWidth }
                        ) + fadeOut(animationSpec = tween(300))
            }
        ) { step ->
            when (step) {
                TransactionStep.FlowSelection -> {
                    TransactionTypeSelector(
                        selectedDirection = transactionInput.direction,
                        availableAccounts = accounts,
                        onTypeSelected = viewModel::onTransactionDirectionSelected,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TransactionStep.Amount -> {
                    AmountInput(
                        amountText = amountText,
                        onAmountTextChanged = { amountText = it },
                        onAmountChanged = viewModel::onAmountChanged,
                        onNextClicked = viewModel::moveToNextStep,
                        title = when (transactionInput.direction) {
                            TransactionDirection.Inflow -> "Wie viel Geld hast du erhalten?"
                            TransactionDirection.Outflow -> "Wie viel Geld hast du ausgegeben?"
                            TransactionDirection.AccountTransfer -> "Wie viel Geld möchtest du transferieren?"
                            else -> "Gib den Betrag ein"
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TransactionStep.FromAccount -> {
                    AccountSelector(
                        title = getStepTitle(step, transactionInput),
                        accounts = accounts,
                        selectedAccount = transactionInput.fromAccount,
                        onAccountSelected = viewModel::onFromAccountSelected,
                        onCreateAccountClicked = onShowAddAccountDialog,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TransactionStep.ToAccount -> {
                    AccountSelector(
                        title = getStepTitle(step, transactionInput),
                        accounts = accounts.filter { it.id != transactionInput.fromAccount?.id },
                        selectedAccount = transactionInput.toAccount,
                        onAccountSelected = viewModel::onToAccountSelected,
                        onCreateAccountClicked = onShowAddAccountDialog,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TransactionStep.Partner -> {
                    TextInput(
                        title = when (transactionInput.direction) {
                            TransactionDirection.Inflow -> "Von wem hast du das Geld erhalten?"
                            TransactionDirection.Outflow -> "Wo hast du das Geld ausgegeben?"
                            else -> "Partner eingeben"
                        },
                        value = transactionInput.partner,
                        onValueChange = viewModel::onPartnerChanged,
                        onNextClicked = viewModel::moveToNextStep,
                        placeholder = when (transactionInput.direction) {
                            TransactionDirection.Inflow -> "z.B. Arbeitgeber, Familie, Kunde"
                            TransactionDirection.Outflow -> "z.B. Supermarkt, Restaurant"
                            else -> "Name des Partners"
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TransactionStep.Category -> {
                    CategorySelector(
                        title = "Für welche Kategorie?",
                        categories = categories,
                        selectedCategory = transactionInput.category,
                        onCategorySelected = viewModel::onCategorySelected,
                        onCreateCategoryClicked = onShowAddCategoryDialog,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TransactionStep.Date -> {
                    DateSelector(
                        title = "Wann war das?",
                        selectedDate = transactionInput.date,
                        showDatePicker = showDatePicker,
                        onDateSelected = viewModel::onDateSelected,
                        onShowDatePickerChanged = { showDatePicker = it },
                        onNextClicked = viewModel::moveToNextStep,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TransactionStep.Optional -> {
                    TextInput(
                        title = "Möchtest du eine Notiz hinzufügen?",
                        value = transactionInput.description,
                        onValueChange = viewModel::onDescriptionChanged,
                        onNextClicked = { viewModel.saveTransaction() },
                        placeholder = "Notiz (optional)",
                        nextButtonText = "✨ Transaktion speichern",
                        isRequired = false,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                TransactionStep.Done -> {
                    CompletionStep(
                        onSwitchToStandardMode = viewModel::switchToStandardMode,
                        title = when (transactionInput.direction) {
                            TransactionDirection.Inflow -> "💰 Einnahme gespeichert!"
                            TransactionDirection.Outflow -> "💸 Ausgabe gespeichert!"
                            TransactionDirection.AccountTransfer -> "🔄 Transfer abgeschlossen!"
                            else -> "✅ Transaktion gespeichert!"
                        },
                        message = "Deine Transaktion wurde erfolgreich hinzugefügt.\nDein Kontostand wurde entsprechend aktualisiert.",
                        buttonText = "Weitere Transaktion hinzufügen",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * Standard Mode: Multi-step form with all relevant fields visible
 */
@Composable
private fun StandardFormContent(
    transactionInput: TransactionInput,
    categories: List<app.tinygiants.getalife.domain.model.Category>,
    accounts: List<app.tinygiants.getalife.domain.model.Account>,
    viewModel: AddTransactionViewModel,
    onShowAddAccountDialog: () -> Unit,
    onShowAddCategoryDialog: () -> Unit
) {
    // Animation state for flow selection
    val isFlowSelected = transactionInput.direction != null &&
            transactionInput.direction != TransactionDirection.Unknown

    AnimatedContent(
        targetState = isFlowSelected,
        label = "Standard Mode Flow Animation",
        transitionSpec = {
            slideInVertically(
                animationSpec = tween(400),
                initialOffsetY = { fullHeight -> if (targetState) -fullHeight else 0 }
            ) + fadeIn(animationSpec = tween(400)) togetherWith
                    slideOutVertically(
                        animationSpec = tween(400),
                        targetOffsetY = { fullHeight -> if (targetState) -fullHeight else fullHeight }
                    ) + fadeOut(animationSpec = tween(400))
        }
    ) { flowSelected ->
        if (!flowSelected) {
            // Centered Flow Selection (like in Guided Mode)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                TransactionTypeSelector(
                    selectedDirection = transactionInput.direction,
                    availableAccounts = accounts,
                    onTypeSelected = viewModel::onTransactionDirectionSelected,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            // Full Form with Flow Selection at top
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(spacing.m)
            ) {
                // Flow Selection (compact, at top)
                TransactionFlowSelector(
                    selectedDirection = transactionInput.direction,
                    onDirectionSelected = viewModel::onTransactionDirectionSelected,
                    isCompact = true,
                    accounts = accounts,
                    modifier = Modifier.fillMaxWidth()
                )

                // Amount Input
                TransactionAmountInput(
                    amount = transactionInput.amount,
                    onAmountChanged = viewModel::onAmountChanged,
                    transactionDirection = transactionInput.direction,
                    isCompact = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // From Account
                TransactionAccountSelector(
                    accounts = accounts,
                    selectedAccount = transactionInput.fromAccount,
                    onAccountSelected = viewModel::onFromAccountSelected,
                    onCreateAccountClicked = onShowAddAccountDialog,
                    title = getStepTitle(TransactionStep.FromAccount, transactionInput),
                    isCompact = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // To Account (only for transfers)
                if (transactionInput.direction == TransactionDirection.AccountTransfer) {
                    TransactionAccountSelector(
                        accounts = accounts,
                        selectedAccount = transactionInput.toAccount,
                        onAccountSelected = viewModel::onToAccountSelected,
                        onCreateAccountClicked = onShowAddAccountDialog,
                        title = getStepTitle(TransactionStep.ToAccount, transactionInput),
                        excludeAccount = transactionInput.fromAccount,
                        isCompact = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Partner (not for transfers)
                if (transactionInput.direction != TransactionDirection.AccountTransfer) {
                    TransactionPartnerInput(
                        partner = transactionInput.partner,
                        onPartnerChanged = viewModel::onPartnerChanged,
                        transactionDirection = transactionInput.direction,
                        isCompact = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Category (only for outflow)
                if (transactionInput.direction == TransactionDirection.Outflow) {
                    TransactionCategorySelector(
                        categories = categories,
                        selectedCategory = transactionInput.category,
                        onCategorySelected = viewModel::onCategorySelected,
                        onCreateCategoryClicked = onShowAddCategoryDialog,
                        isCompact = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Date
                TransactionDateSelector(
                    selectedDate = transactionInput.date,
                    onDateSelected = viewModel::onDateSelected,
                    isCompact = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Description (optional)
                TransactionDescriptionInput(
                    description = transactionInput.description,
                    onDescriptionChanged = viewModel::onDescriptionChanged,
                    isCompact = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(spacing.l))

                // Save Button
                Button(
                    onClick = { viewModel.saveTransaction() },
                    enabled = transactionInput.isValidForCurrentFlow(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Transaktion speichern",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Completion screen shown after successful transaction creation
 */
@Composable
private fun CompletionScreen(
    transactionInput: TransactionInput,
    onSwitchToStandardMode: () -> Unit,
    onCreateNewTransaction: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Celebration emoji
        Text(
            text = "🎉",
            style = MaterialTheme.typography.displayLarge
        )

        Spacer(modifier = Modifier.height(spacing.l))

        // Success message
        Text(
            text = "Fantastisch!",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(spacing.s))

        Text(
            text = "Du hast deine erste Transaktion erfolgreich erstellt!",
            style = MaterialTheme.typography.titleLarge,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(spacing.m))

        // Transaction summary
        if (transactionInput.amount != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(spacing.xs)
            ) {
                Text(
                    text = transactionInput.amount.formattedMoney,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                if (transactionInput.partner.isNotBlank()) {
                    Text(
                        text = transactionInput.partner,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                Text(
                    text = when (transactionInput.direction) {
                        TransactionDirection.Inflow -> "💰 Einnahme"
                        TransactionDirection.Outflow -> "💸 Ausgabe"
                        TransactionDirection.AccountTransfer -> "🔄 Transfer"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(spacing.xl))

        // Motivational message
        Text(
            text = "Du kennst dich jetzt mit Transaktionen aus! 💪",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(spacing.l))

        // Action buttons
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(spacing.m)
        ) {
            Button(
                onClick = onCreateNewTransaction,
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Noch eine Transaktion hinzufügen",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = onSwitchToStandardMode,
                modifier = Modifier.fillMaxWidth(0.8f),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Zum Expertenmodus wechseln",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

// ================================
// Preview Composables
// ================================

@Preview(name = "Add Transaction - Flow Selection", showBackground = true)
@Composable
private fun AddTransactionFlowSelectionPreview() {
    GetALifeTheme {
        TransactionFlowSelector(
            selectedDirection = null,
            onDirectionSelected = { },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Add Transaction - Completion", showBackground = true)
@Composable
private fun AddTransactionCompletionPreview() {
    GetALifeTheme {
        CompletionScreen(
            transactionInput = TransactionInput(
                amount = app.tinygiants.getalife.domain.model.Money(25.50),
                partner = "Edeka"
            ),
            onSwitchToStandardMode = { },
            onCreateNewTransaction = { }
        )
    }
}