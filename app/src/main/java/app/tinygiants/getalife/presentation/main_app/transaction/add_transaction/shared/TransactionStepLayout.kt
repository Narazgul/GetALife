package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.TransactionInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.TransactionStep
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.composables.waveAnimationBackground
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.onSuccess
import app.tinygiants.getalife.theme.spacing

@Composable
fun TransactionStepContainer(
    transactionInput: TransactionInput,
    currentStep: TransactionStep,
    isGuidedMode: Boolean,
    modifier: Modifier = Modifier,
    showProgress: Boolean = true,
    content: @Composable () -> Unit
) {
    val neutralBackground = MaterialTheme.colorScheme.primary.toArgb()
    val inflowBackground = onSuccess.toArgb()
    val outflowBackground = MaterialTheme.colorScheme.errorContainer.toArgb()
    val transferBackground = MaterialTheme.colorScheme.primary.toArgb()

    val waveColor = when (transactionInput.direction) {
        TransactionDirection.Inflow -> inflowBackground
        TransactionDirection.Outflow -> outflowBackground
        TransactionDirection.AccountTransfer -> transferBackground
        else -> neutralBackground
    }


    Scaffold(modifier = modifier) { innerPadding ->
        Box(
            modifier = Modifier
                .consumeWindowInsets(innerPadding)
                .fillMaxSize()
        ) {
            // Animated Wave Background - consistent across all modes
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(450.dp) // Consistent height for all screens
                    .waveAnimationBackground(color = waveColor)
            )

            // Main content container with better spacing for guided mode
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = spacing.m)
                    .padding(
                        WindowInsets.statusBars.asPaddingValues()
                    )
            ) {
                // Progress indicator for guided mode - stays at top
                if (isGuidedMode && showProgress) {
                    TransactionProgressIndicator(
                        currentStep = currentStep,
                        transactionInput = transactionInput,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = spacing.s)
                    )
                }

                // Step content - centered for guided mode, normal for standard mode
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(
                            top = if (isGuidedMode) spacing.l else spacing.m,
                            bottom = spacing.m
                        ),
                    contentAlignment = if (isGuidedMode) Alignment.Center else Alignment.TopCenter
                ) {
                    content()
                }
            }
        }
    }
}

/**
 * Calculates the progress percentage for the current step.
 * Used by TransactionProgressIndicator.
 */
fun calculateStepProgress(
    currentStep: TransactionStep,
    transactionInput: TransactionInput
): Float {
    // Total steps vary by transaction type
    val totalSteps = when (transactionInput.direction) {
        TransactionDirection.Inflow -> 6 // FlowSelection, Amount, FromAccount, Partner, Date, Optional
        TransactionDirection.Outflow -> 7 // FlowSelection, Amount, FromAccount, Partner, Category, Date, Optional
        TransactionDirection.AccountTransfer -> 6 // FlowSelection, Amount, FromAccount, ToAccount, Date, Optional
        else -> 8 // Default maximum
    }

    val currentStepIndex = when (currentStep) {
        TransactionStep.FlowSelection -> 0
        TransactionStep.Amount -> 1
        TransactionStep.FromAccount -> 2
        TransactionStep.ToAccount -> 3 // Only for transfers
        TransactionStep.Partner -> 3 // For inflow/outflow (skip ToAccount)
        TransactionStep.Category -> 4 // Only for outflow
        TransactionStep.Date -> when (transactionInput.direction) {
            TransactionDirection.AccountTransfer -> 4
            TransactionDirection.Inflow -> 4
            TransactionDirection.Outflow -> 5
            else -> 5
        }

        TransactionStep.Optional -> totalSteps - 1
        TransactionStep.Done -> totalSteps
    }

    return (currentStepIndex.toFloat() / totalSteps).coerceIn(0f, 1f)
}

/**
 * Returns the current step title for display.
 */
fun getStepTitle(step: TransactionStep, transactionInput: TransactionInput): String {
    return when (step) {
        TransactionStep.FlowSelection -> "Was möchten Sie tun?"
        TransactionStep.Amount -> when (transactionInput.direction) {
            TransactionDirection.Inflow -> "Wie viel haben Sie erhalten?"
            TransactionDirection.Outflow -> "Wie viel haben Sie ausgegeben?"
            TransactionDirection.AccountTransfer -> "Wie viel möchten Sie überweisen?"
            else -> "Betrag eingeben"
        }

        TransactionStep.FromAccount -> when (transactionInput.direction) {
            TransactionDirection.Inflow -> "Auf welches Konto?"
            TransactionDirection.Outflow -> "Von welchem Konto?"
            TransactionDirection.AccountTransfer -> "Von welchem Konto?"
            else -> "Konto auswählen"
        }

        TransactionStep.ToAccount -> "Auf welches Konto überweisen?"
        TransactionStep.Partner -> when (transactionInput.direction) {
            TransactionDirection.Inflow -> "Von wem haben Sie Geld erhalten?"
            TransactionDirection.Outflow -> "Wo haben Sie das Geld ausgegeben?"
            else -> "Partner eingeben"
        }

        TransactionStep.Category -> "Für welche Kategorie?"
        TransactionStep.Date -> "Wann war das?"
        TransactionStep.Optional -> "Möchten Sie eine Notiz hinzufügen?"
        TransactionStep.Done -> "Geschafft! 🎉"
    }
}

/**
 * Returns motivational progress text for the current step.
 */
fun getProgressText(step: TransactionStep, transactionInput: TransactionInput): String {
    return when (step) {
        TransactionStep.FlowSelection -> "Los geht's! Was möchtest du tun? 🚀"

        TransactionStep.Amount -> when (transactionInput.direction) {
            TransactionDirection.Inflow -> "Super! Wie viel Geld hast du erhalten? 💰"
            TransactionDirection.Outflow -> "Perfekt! Wie viel hast du ausgegeben? 💸"
            TransactionDirection.AccountTransfer -> "Toll! Wie viel möchtest du transferieren? 🔄"
            else -> "Großartig! Gib den Betrag ein 💪"
        }

        TransactionStep.FromAccount -> when (transactionInput.direction) {
            TransactionDirection.Inflow -> "Fast geschafft! Auf welches Konto? 🏦"
            TransactionDirection.Outflow -> "Weiter so! Von welchem Konto? 🏦"
            TransactionDirection.AccountTransfer -> "Prima! Von welchem Konto? 🏦"
            else -> "Gut! Wähle dein Konto 🏦"
        }

        TransactionStep.ToAccount -> "Klasse! Wohin soll das Geld? 🎯"

        TransactionStep.Partner -> when (transactionInput.direction) {
            TransactionDirection.Inflow -> "Fantastisch! Von wem war das? 👤"
            TransactionDirection.Outflow -> "Sehr gut! Wo warst du einkaufen? 🛍️"
            else -> "Super! Wer war dein Partner? 👤"
        }

        TransactionStep.Category -> "Ausgezeichnet! Für welche Kategorie? 📂"

        TransactionStep.Date -> "Fast fertig! Wann war das? 📅"

        TransactionStep.Optional -> "Letzter Schritt! Möchtest du eine Notiz hinzufügen? ✏️"

        TransactionStep.Done -> "Perfekt! Du hast alles geschafft! 🎉"
    }
}

@Preview(name = "Guided Mode - Inflow", showBackground = true)
@Composable
private fun TransactionStepContainerGuidedInflowPreview() {
    GetALifeTheme {
        TransactionStepContainer(
            transactionInput = TransactionInput(
                direction = TransactionDirection.Inflow,
                amount = Money(123.45)
            ),
            currentStep = TransactionStep.Amount,
            isGuidedMode = true
        ) {
            Text(
                text = "Guided Mode - Inflow\n(Betrag eingeben)",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(name = "Guided Mode - Outflow (Category)", showBackground = true)
@Composable
private fun TransactionStepContainerGuidedOutflowPreview() {
    GetALifeTheme {
        TransactionStepContainer(
            transactionInput = TransactionInput(
                direction = TransactionDirection.Outflow,
                amount = Money(20.50)
            ),
            currentStep = TransactionStep.Category,
            isGuidedMode = true
        ) {
            Text(
                text = "Guided Mode - Outflow\n(Kategorie auswählen)",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(name = "Standard Mode - Account Transfer", showBackground = true)
@Composable
private fun TransactionStepContainerStandardTransferPreview() {
    GetALifeTheme {
        TransactionStepContainer(
            transactionInput = TransactionInput(
                direction = TransactionDirection.AccountTransfer,
                amount = Money(500.0)
            ),
            currentStep = TransactionStep.Date,
            isGuidedMode = false
        ) {
            Text(
                text = "Standard Mode\n(Überweisung - Datum wählen)",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
        }
    }
}