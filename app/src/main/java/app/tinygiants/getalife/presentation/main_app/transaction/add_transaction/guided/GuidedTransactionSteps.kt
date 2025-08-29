package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.AddTransactionUiState
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.AddTransactionViewModel
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.GuidedTransactionStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.InflowAccountStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.InflowAmountStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.InflowCompletionStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.InflowDateStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.InflowOptionalStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.InflowPartnerStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.InflowTypeStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.OutflowAccountStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.OutflowAmountStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.OutflowCategoryStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.OutflowCompletionStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.OutflowDateStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.OutflowOptionalStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.OutflowPartnerStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.OutflowTypeStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.TransferAmountStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.TransferCompletionStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.TransferDateStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.TransferFromAccountStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.TransferOptionalStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.TransferToAccountStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.steps.TransferTypeStep
import app.tinygiants.getalife.theme.GetALifeTheme
import kotlin.time.Clock

/**
 * Refactored guided transaction steps using modular components.
 * Each step is now flow-specific and uses reusable UI components.
 *
 * This replaces the original monolithic GuidedTransactionSteps.kt (851 lines)
 * with a clean, modular approach that's easier to maintain and test.
 */

// ================================
// FLOW ORCHESTRATION
// ================================

/**
 * Main step orchestrator that routes to the appropriate flow-specific step.
 * Uses the current transaction direction to determine which step components to show.
 */
@Composable
fun GuidedTransactionStep(
    step: GuidedTransactionStep,
    uiState: AddTransactionUiState,
    viewModel: AddTransactionViewModel,
    modifier: Modifier = Modifier
) {
    // Local state for UI interactions
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var amountText by rememberSaveable { mutableStateOf(uiState.selectedAmount?.asDouble()?.toString() ?: "") }

    when (step) {
        // Step 1: Transaction Type (shared by all flows)
        GuidedTransactionStep.Type -> {
            when (uiState.selectedDirection) {
                TransactionDirection.Inflow -> InflowTypeStep(
                    selectedDirection = uiState.selectedDirection,
                    availableAccounts = uiState.accounts,
                    onTypeSelected = viewModel::onGuidedTransactionTypeSelected,
                    modifier = modifier
                )

                TransactionDirection.Outflow -> OutflowTypeStep(
                    selectedDirection = uiState.selectedDirection,
                    availableAccounts = uiState.accounts,
                    onTypeSelected = viewModel::onGuidedTransactionTypeSelected,
                    modifier = modifier
                )

                TransactionDirection.Unknown -> TransferTypeStep( // Transfer
                    selectedDirection = uiState.selectedDirection,
                    availableAccounts = uiState.accounts,
                    onTypeSelected = viewModel::onGuidedTransactionTypeSelected,
                    modifier = modifier
                )

                else -> InflowTypeStep( // Default to Inflow if nothing selected
                    selectedDirection = uiState.selectedDirection,
                    availableAccounts = uiState.accounts,
                    onTypeSelected = viewModel::onGuidedTransactionTypeSelected,
                    modifier = modifier
                )
            }
        }

        // Step 2: Amount Input (flow-specific titles)
        GuidedTransactionStep.Amount -> {
            when (uiState.selectedDirection) {
                TransactionDirection.Inflow -> InflowAmountStep(
                    amountText = amountText,
                    onAmountTextChanged = { amountText = it },
                    onAmountChanged = viewModel::onGuidedAmountEntered,
                    onNextClicked = viewModel::moveToNextStep,
                    modifier = modifier
                )

                TransactionDirection.Outflow -> OutflowAmountStep(
                    amountText = amountText,
                    onAmountTextChanged = { amountText = it },
                    onAmountChanged = viewModel::onGuidedAmountEntered,
                    onNextClicked = viewModel::moveToNextStep,
                    modifier = modifier
                )

                TransactionDirection.Unknown -> TransferAmountStep( // Transfer
                    amountText = amountText,
                    onAmountTextChanged = { amountText = it },
                    onAmountChanged = viewModel::onGuidedAmountEntered,
                    onNextClicked = viewModel::moveToNextStep,
                    modifier = modifier
                )

                else -> InflowAmountStep(
                    amountText = amountText,
                    onAmountTextChanged = { amountText = it },
                    onAmountChanged = viewModel::onGuidedAmountEntered,
                    onNextClicked = viewModel::moveToNextStep,
                    modifier = modifier
                )
            }
        }

        // Step 3: Account Selection (from account for all flows)
        GuidedTransactionStep.Account -> {
            when (uiState.selectedDirection) {
                TransactionDirection.Inflow -> InflowAccountStep(
                    accounts = uiState.accounts,
                    selectedAccount = uiState.selectedAccount,
                    onAccountSelected = viewModel::onGuidedAccountSelected,
                    onCreateAccountClicked = { /* TODO: Show account creation dialog */ },
                    modifier = modifier
                )

                TransactionDirection.Outflow -> OutflowAccountStep(
                    accounts = uiState.accounts,
                    selectedAccount = uiState.selectedAccount,
                    onAccountSelected = viewModel::onGuidedAccountSelected,
                    onCreateAccountClicked = { /* TODO: Show account creation dialog */ },
                    modifier = modifier
                )

                TransactionDirection.Unknown -> TransferFromAccountStep( // Transfer
                    accounts = uiState.accounts,
                    selectedAccount = uiState.selectedAccount,
                    onAccountSelected = viewModel::onGuidedAccountSelected,
                    onCreateAccountClicked = { /* TODO: Show account creation dialog */ },
                    modifier = modifier
                )

                else -> InflowAccountStep(
                    accounts = uiState.accounts,
                    selectedAccount = uiState.selectedAccount,
                    onAccountSelected = viewModel::onGuidedAccountSelected,
                    onCreateAccountClicked = { /* TODO: Show account creation dialog */ },
                    modifier = modifier
                )
            }
        }

        // Step 4: To Account Selection (only for transfers)
        GuidedTransactionStep.ToAccount -> {
            TransferToAccountStep(
                accounts = uiState.accounts.filter { it != uiState.selectedAccount },
                selectedToAccount = uiState.selectedToAccount,
                onToAccountSelected = viewModel::onGuidedToAccountSelected,
                modifier = modifier
            )
        }

        // Step 5: Partner Input (skipped for transfers)
        GuidedTransactionStep.Partner -> {
            when (uiState.selectedDirection) {
                TransactionDirection.Inflow -> InflowPartnerStep(
                    currentPartner = uiState.selectedPartner,
                    onPartnerChanged = viewModel::onGuidedPartnerEntered,
                    onNextClicked = viewModel::moveToNextStep,
                    modifier = modifier
                )

                TransactionDirection.Outflow -> OutflowPartnerStep(
                    currentPartner = uiState.selectedPartner,
                    onPartnerChanged = viewModel::onGuidedPartnerEntered,
                    onNextClicked = viewModel::moveToNextStep,
                    modifier = modifier
                )

                else -> InflowPartnerStep(
                    currentPartner = uiState.selectedPartner,
                    onPartnerChanged = viewModel::onGuidedPartnerEntered,
                    onNextClicked = viewModel::moveToNextStep,
                    modifier = modifier
                )
            }
        }

        // Step 6: Category Selection (only for outflow)
        GuidedTransactionStep.Category -> {
            OutflowCategoryStep(
                categories = uiState.categories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::onGuidedCategorySelected,
                onCreateCategoryClicked = { /* TODO: Show category creation dialog */ },
                modifier = modifier
            )
        }

        // Step 7: Date Selection (flow-specific titles)
        GuidedTransactionStep.Date -> {
            when (uiState.selectedDirection) {
                TransactionDirection.Inflow -> InflowDateStep(
                    selectedDate = uiState.selectedDate,
                    showDatePicker = showDatePicker,
                    onDateSelected = viewModel::onGuidedDateSelected,
                    onShowDatePickerChanged = { showDatePicker = it },
                    onNextClicked = viewModel::moveToNextStep,
                    modifier = modifier
                )

                TransactionDirection.Outflow -> OutflowDateStep(
                    selectedDate = uiState.selectedDate,
                    showDatePicker = showDatePicker,
                    onDateSelected = viewModel::onGuidedDateSelected,
                    onShowDatePickerChanged = { showDatePicker = it },
                    onNextClicked = viewModel::moveToNextStep,
                    modifier = modifier
                )

                TransactionDirection.Unknown -> TransferDateStep( // Transfer
                    selectedDate = uiState.selectedDate,
                    showDatePicker = showDatePicker,
                    onDateSelected = viewModel::onGuidedDateSelected,
                    onShowDatePickerChanged = { showDatePicker = it },
                    onNextClicked = viewModel::moveToNextStep,
                    modifier = modifier
                )

                else -> InflowDateStep(
                    selectedDate = uiState.selectedDate,
                    showDatePicker = showDatePicker,
                    onDateSelected = viewModel::onGuidedDateSelected,
                    onShowDatePickerChanged = { showDatePicker = it },
                    onNextClicked = viewModel::moveToNextStep,
                    modifier = modifier
                )
            }
        }

        // Step 8: Optional Details (flow-specific button text)
        GuidedTransactionStep.Optional -> {
            when (uiState.selectedDirection) {
                TransactionDirection.Inflow -> InflowOptionalStep(
                    description = uiState.selectedDescription,
                    onDescriptionChanged = viewModel::onGuidedDescriptionChanged,
                    onFinishClicked = viewModel::onGuidedTransactionComplete,
                    modifier = modifier
                )

                TransactionDirection.Outflow -> OutflowOptionalStep(
                    description = uiState.selectedDescription,
                    onDescriptionChanged = viewModel::onGuidedDescriptionChanged,
                    onFinishClicked = viewModel::onGuidedTransactionComplete,
                    modifier = modifier
                )

                TransactionDirection.Unknown -> TransferOptionalStep( // Transfer
                    description = uiState.selectedDescription,
                    onDescriptionChanged = viewModel::onGuidedDescriptionChanged,
                    onFinishClicked = viewModel::onGuidedTransactionComplete,
                    modifier = modifier
                )

                else -> InflowOptionalStep(
                    description = uiState.selectedDescription,
                    onDescriptionChanged = viewModel::onGuidedDescriptionChanged,
                    onFinishClicked = viewModel::onGuidedTransactionComplete,
                    modifier = modifier
                )
            }
        }

        // Step 9: Completion (flow-specific celebration)
        GuidedTransactionStep.Done -> {
            when (uiState.selectedDirection) {
                TransactionDirection.Inflow -> InflowCompletionStep(
                    onSwitchToStandardMode = viewModel::switchToStandardMode,
                    modifier = modifier
                )

                TransactionDirection.Outflow -> OutflowCompletionStep(
                    onSwitchToStandardMode = viewModel::switchToStandardMode,
                    modifier = modifier
                )

                TransactionDirection.Unknown -> TransferCompletionStep( // Transfer
                    onSwitchToStandardMode = viewModel::switchToStandardMode,
                    modifier = modifier
                )

                else -> InflowCompletionStep(
                    onSwitchToStandardMode = viewModel::switchToStandardMode,
                    modifier = modifier
                )
            }
        }
    }
}

// ================================
// COMPLETED STEPS CHECKLIST
// ================================

/**
 * Shows completed steps in a compact checklist format.
 * Allows users to go back and edit previous steps.
 */
@Composable
fun CompletedStepsChecklist(
    uiState: AddTransactionUiState,
    onStepClicked: (GuidedTransactionStep) -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = GuidedTransactionStep.entries

    // Filter out steps not relevant to current flow
    val relevantSteps = steps.filter { step ->
        when (step) {
            GuidedTransactionStep.ToAccount -> uiState.selectedDirection == TransactionDirection.Unknown
            GuidedTransactionStep.Partner -> uiState.selectedDirection != TransactionDirection.Unknown
            GuidedTransactionStep.Category -> uiState.selectedDirection == TransactionDirection.Outflow
            else -> true
        }
    }

    Column(
        modifier = modifier
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
                    text = "${step.getLocalizedName()}: ${getStepDisplayValue(step, uiState)}",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ================================
// HELPER FUNCTIONS
// ================================

/**
 * Get display value for completed step
 */
private fun getStepDisplayValue(step: GuidedTransactionStep, uiState: AddTransactionUiState): String {
    return when (step) {
        GuidedTransactionStep.Type -> when (uiState.selectedDirection) {
            TransactionDirection.Inflow -> "Einnahme"
            TransactionDirection.Outflow -> "Ausgabe"
            TransactionDirection.Unknown -> "Transfer"
            else -> ""
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

/**
 * Get localized step name
 */
private fun GuidedTransactionStep.getLocalizedName(): String = when (this) {
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

// ================================
// PREVIEW
// ================================

@Preview(name = "Guided Step - Outflow Category Selection", showBackground = true)
@Composable
private fun GuidedStepOutflowCategoryPreview() {
    GetALifeTheme {
        val mockUiState = AddTransactionUiState(
            selectedDirection = TransactionDirection.Outflow,
            categories = listOf(
                Category(
                    id = 1L, groupId = 1L, emoji = "🍕", name = "Lebensmittel",
                    budgetTarget = Money(400.0), monthlyTargetAmount = null, targetMonthsRemaining = null,
                    listPosition = 1, isInitialCategory = false, linkedAccountId = null,
                    updatedAt = Clock.System.now(), createdAt = Clock.System.now()
                )
            ),
            guidedStep = GuidedTransactionStep.Category
        )

        OutflowCategoryStep(
            categories = mockUiState.categories,
            selectedCategory = null,
            onCategorySelected = { },
            onCreateCategoryClicked = { }
        )
    }
}