package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.AddTransactionUiState
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.AddTransactionViewModel
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.GuidedTransactionStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.composables.waveAnimationBackground
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.onSuccess
import app.tinygiants.getalife.theme.spacing

/**
 * Main guided transaction flow screen with progress tracking and step orchestration.
 * Performance optimized - receives ViewModel as parameter to avoid multiple injections.
 */
@Composable
fun GuidedTransactionScreen(
    viewModel: AddTransactionViewModel,
    uiState: AddTransactionUiState
) {
    // Collect additional states needed for guided mode
    val partnerSuggestions by viewModel.partners.collectAsStateWithLifecycle()
    val smartCategorizationState by viewModel.smartCategorizationState.collectAsStateWithLifecycle()

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
                // Enhanced Progress Header with better design
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            WindowInsets.statusBars.asPaddingValues()
                        )
                        .padding(bottom = spacing.s),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Improved progress indicator
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(8.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(spacing.s))

                    // Motivational text with better styling
                    Text(
                        text = getProgressText(uiState.guidedStep),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier
                            .background(
                                color = Color.Black.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
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
                                availableAccounts = uiState.accounts,
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
                                onAccountSelected = viewModel::onGuidedAccountSelected,
                                onCreateAccountClicked = { /* Dialog handling inside step */ }
                            )

                            GuidedTransactionStep.ToAccount -> ToAccountSelectionStep(
                                accounts = uiState.accounts.filter { it != uiState.selectedAccount }, // Exclude source account
                                selectedToAccount = uiState.selectedToAccount,
                                onToAccountSelected = viewModel::onGuidedToAccountSelected
                            )

                            GuidedTransactionStep.Partner -> PartnerInputStep(
                                currentPartner = uiState.selectedPartner,
                                partnerSuggestions = partnerSuggestions,
                                selectedDirection = uiState.selectedDirection,
                                onPartnerChanged = viewModel::onGuidedPartnerEntered,
                                onNextClicked = viewModel::moveToNextStep
                            )

                            GuidedTransactionStep.Category -> CategorySelectionStep(
                                categories = uiState.categories,
                                selectedCategory = uiState.selectedCategory,
                                smartCategorizationState = smartCategorizationState,
                                onCategorySelected = viewModel::onGuidedCategorySelected,
                                onCreateCategoryClicked = { /* Dialog handling inside step */ },
                                onAISuggestionAccepted = viewModel::onCategorySuggestionAccepted,
                                onNewAICategoryCreated = viewModel::onCreateCategoryFromSuggestion
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

                            GuidedTransactionStep.Done -> TransactionCompletedStep(
                                onSwitchToStandardMode = viewModel::switchToStandardMode
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper function for progress text based on current step.
 * Pure function for better testability.
 */
fun getProgressText(guidedStep: GuidedTransactionStep): String {
    return when (guidedStep) {
        GuidedTransactionStep.Type -> "Beginnen wir mit den Grundlagen 💪"
        GuidedTransactionStep.Amount -> "Super, weiter so! Noch 6 Schritte 🚀"
        GuidedTransactionStep.Account -> "Noch 5 Schritte 📈"
        GuidedTransactionStep.ToAccount -> "Wohin soll das Geld? 💸"
        GuidedTransactionStep.Partner -> "Noch 4 Schritte ⭐"
        GuidedTransactionStep.Category -> "Fast geschafft! 🎯"
        GuidedTransactionStep.Date -> "Vorletzter Schritt! 📅"
        GuidedTransactionStep.Optional -> "Letzter Schritt! 🏁"
        GuidedTransactionStep.Done -> "Stark! Du hast alle Schritte abgeschlossen 🎉"
    }
}

/**
 * Helper functions for step values and localization.
 * Moved here from main screen for better organization.
 */
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

// ================================
// Preview Composables
// ================================

/**
 * Preview of individual guided steps to show the flow concept
 */
@Preview(name = "Guided Flow - Type Selection", showBackground = true)
@Composable
private fun GuidedTransactionTypePreview() {
    GetALifeTheme {
        TransactionTypeStep(
            selectedDirection = TransactionDirection.Inflow,
            availableAccounts = emptyList(), // No transfer option shown
            onTypeSelected = { }
        )
    }
}

@Preview(name = "Guided Flow - Amount Input", showBackground = true)
@Composable
private fun GuidedTransactionAmountPreview() {
    GetALifeTheme {
        AmountInputStep(
            currentAmount = null,
            onAmountChanged = { },
            onNextClicked = { }
        )
    }
}

@Preview(name = "Guided Flow - Completed Step Checklist", showBackground = true)
@Composable
private fun GuidedCompletedStepsPreview() {
    GetALifeTheme {
        val mockUiState = AddTransactionUiState(
            isGuidedMode = true,
            guidedStep = GuidedTransactionStep.Category,
            selectedDirection = TransactionDirection.Outflow,
            selectedAmount = Money(25.99),
            selectedAccount = null,
            selectedPartner = "Edeka"
        )

        CompletedStepsChecklist(
            uiState = mockUiState,
            onStepClicked = { }
        )
    }
}