package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.flows.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.TransactionInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.TransactionStep
import app.tinygiants.getalife.theme.spacing
import java.time.LocalDate

/**
 * Progress indicator for onboarding transaction flows with motivational text.
 */
@Composable
fun OnboardingTransactionProgress(
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
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(8.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            trackColor = Color.White.copy(alpha = 0.3f)
        )

        Spacer(modifier = Modifier.height(spacing.s))

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
 * Compact version for situations where space is limited.
 */
@Composable
fun CompactTransactionProgress(
    currentStep: TransactionStep,
    transactionInput: TransactionInput,
    modifier: Modifier = Modifier
) {
    val progress = calculateStepProgress(currentStep, transactionInput)

    LinearProgressIndicator(
        progress = { progress },
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
}

@Composable
fun CompletedStepsChecklist(
    transactionInput: TransactionInput,
    currentStep: TransactionStep,
    onStepClicked: (TransactionStep) -> Unit,
    modifier: Modifier = Modifier
) {
    val allSteps = TransactionStep.entries.filter { it != TransactionStep.Done }

    val relevantSteps = allSteps.filter { step ->
        when (step) {
            TransactionStep.ToAccount -> transactionInput.direction == TransactionDirection.AccountTransfer
            TransactionStep.Partner -> transactionInput.direction != TransactionDirection.AccountTransfer
            TransactionStep.Category -> transactionInput.direction == TransactionDirection.Outflow
            else -> true
        }
    }

    val completedSteps = relevantSteps.takeWhile { it != currentStep }

    if (completedSteps.isNotEmpty()) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = spacing.m),
            verticalArrangement = Arrangement.spacedBy(spacing.xs)
        ) {
            completedSteps.forEach { step ->
                CompletedStepItem(
                    step = step,
                    transactionInput = transactionInput,
                    onClick = { onStepClicked(step) }
                )
            }
        }
    }
}

@Composable
private fun CompletedStepItem(
    step: TransactionStep,
    transactionInput: TransactionInput,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stepValue = getStepDisplayValue(step, transactionInput)

    if (stepValue.isNotEmpty()) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(spacing.s)
                )
                .clickable { onClick() }
                .padding(vertical = spacing.xs, horizontal = spacing.s),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(spacing.xs))
            Text(
                text = "${step.getLocalizedName()}: $stepValue",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun OnboardingStepCounter(
    currentStep: TransactionStep,
    transactionInput: TransactionInput,
    modifier: Modifier = Modifier
) {
    val totalSteps = when (transactionInput.direction) {
        TransactionDirection.Inflow -> 6
        TransactionDirection.Outflow -> 7
        TransactionDirection.AccountTransfer -> 6
        else -> 8
    }

    val currentStepNumber = when (currentStep) {
        TransactionStep.FlowSelection -> 1
        TransactionStep.Amount -> 2
        TransactionStep.FromAccount -> 3
        TransactionStep.ToAccount -> 4
        TransactionStep.Partner -> if (transactionInput.direction == TransactionDirection.AccountTransfer) 5 else 4
        TransactionStep.Category -> 5
        TransactionStep.Date -> when (transactionInput.direction) {
            TransactionDirection.AccountTransfer -> 5
            TransactionDirection.Inflow -> 5
            TransactionDirection.Outflow -> 6
            else -> 6
        }

        TransactionStep.Optional -> totalSteps
        TransactionStep.Done -> totalSteps
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Schritt $currentStepNumber von $totalSteps",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

// Helper functions
private fun calculateStepProgress(
    currentStep: TransactionStep,
    transactionInput: TransactionInput
): Float {
    val totalSteps = when (transactionInput.direction) {
        TransactionDirection.Inflow -> 6
        TransactionDirection.Outflow -> 7
        TransactionDirection.AccountTransfer -> 6
        else -> 8
    }

    val currentStepIndex = when (currentStep) {
        TransactionStep.FlowSelection -> 0
        TransactionStep.Amount -> 1
        TransactionStep.FromAccount -> 2
        TransactionStep.ToAccount -> 3
        TransactionStep.Partner -> 3
        TransactionStep.Category -> 4
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

private fun getStepDisplayValue(step: TransactionStep, transactionInput: TransactionInput): String {
    return when (step) {
        TransactionStep.FlowSelection -> when (transactionInput.direction) {
            TransactionDirection.Inflow -> "💰 Einnahme"
            TransactionDirection.Outflow -> "💸 Ausgabe"
            TransactionDirection.AccountTransfer -> "🔄 Transfer"
            else -> ""
        }

        TransactionStep.Amount -> transactionInput.amount?.formattedMoney ?: ""
        TransactionStep.FromAccount -> transactionInput.fromAccount?.name ?: ""
        TransactionStep.ToAccount -> transactionInput.toAccount?.name ?: ""
        TransactionStep.Partner -> transactionInput.partner.takeIf { it.isNotBlank() } ?: ""
        TransactionStep.Category -> transactionInput.category?.name ?: ""
        TransactionStep.Date -> transactionInput.date?.let { formatDate(it) } ?: ""
        TransactionStep.Optional -> if (transactionInput.description.isNotEmpty()) "✏️ Beschreibung hinzugefügt" else ""
        TransactionStep.Done -> "✅ Fertig"
    }
}

private fun TransactionStep.getLocalizedName(): String = when (this) {
    TransactionStep.FlowSelection -> "Art"
    TransactionStep.Amount -> "Betrag"
    TransactionStep.FromAccount -> "Konto"
    TransactionStep.ToAccount -> "Zielkonto"
    TransactionStep.Partner -> "Partner"
    TransactionStep.Category -> "Kategorie"
    TransactionStep.Date -> "Datum"
    TransactionStep.Optional -> "Details"
    TransactionStep.Done -> "Fertig"
}

private fun formatDate(date: LocalDate): String {
    return "${date.dayOfMonth.toString().padStart(2, '0')}.${
        date.monthValue.toString().padStart(2, '0')
    }.${date.year}"
}

@Composable
fun CompletionStep(
    onSwitchToStandardMode: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "✅ Transaktion gespeichert!",
    message: String = "Deine Transaktion wurde erfolgreich hinzugefügt.",
    buttonText: String = "Weitere Transaktion hinzufügen"
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(spacing.m))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.9f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(spacing.xl))

        Button(
            onClick = onSwitchToStandardMode,
            modifier = Modifier.fillMaxWidth(0.8f),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = buttonText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}