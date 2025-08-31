package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.TransactionInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.TransactionStep
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing

/**
 * Progress indicator for guided transaction flows with motivational text.
 *
 * Features:
 * - Dynamic progress calculation based on transaction type
 * - Motivational progress text that adapts to current step
 * - Clean, modern design that works with wave background
 * - Consistent styling across all flows
 */
@Composable
fun GuidedTransactionProgress(
    currentStep: TransactionStep,
    transactionInput: TransactionInput,
    modifier: Modifier = Modifier
) {
    val progress = calculateStepProgress(currentStep, transactionInput)
    val progressText = getProgressText(currentStep, transactionInput)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress bar with consistent styling
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(8.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            trackColor = Color.White.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(spacing.s))

        // Motivational progress text with semi-transparent background
        Text(
            text = progressText,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .background(
                    color = Color.Black.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/**
 * Calculates the progress percentage for the current step.
 * Used by progress indicator components in this file.
 */
private fun calculateStepProgress(
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
 * Returns motivational progress text for the current step.
 * Used by progress indicator components in this file.
 */
private fun getProgressText(step: TransactionStep, transactionInput: TransactionInput): String {
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

// ================================
// Preview Composables  
// ================================

@Preview(name = "Progress - Inflow Amount Step", showBackground = true)
@Composable
private fun GuidedTransactionProgressInflowPreview() {
    GetALifeTheme {
        GuidedTransactionProgress(
            currentStep = TransactionStep.Amount,
            transactionInput = TransactionInput(
                direction = TransactionDirection.Inflow
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Progress - Outflow Category Step", showBackground = true)
@Composable
private fun GuidedTransactionProgressOutflowPreview() {
    GetALifeTheme {
        GuidedTransactionProgress(
            currentStep = TransactionStep.Category,
            transactionInput = TransactionInput(
                direction = TransactionDirection.Outflow,
                amount = Money(25.50)
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Progress - Transfer ToAccount Step", showBackground = true)
@Composable
private fun GuidedTransactionProgressTransferPreview() {
    GetALifeTheme {
        GuidedTransactionProgress(
            currentStep = TransactionStep.ToAccount,
            transactionInput = TransactionInput(
                direction = TransactionDirection.AccountTransfer,
                amount = Money(100.0)
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}



@Preview(name = "Progress - Almost Complete", showBackground = true)
@Composable
private fun GuidedTransactionProgressAlmostCompletePreview() {
    GetALifeTheme {
        GuidedTransactionProgress(
            currentStep = TransactionStep.Optional,
            transactionInput = TransactionInput(
                direction = TransactionDirection.Outflow,
                amount = Money(42.99)
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}