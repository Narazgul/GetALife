package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.shared

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.TransactionDirection
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.TransactionInput
import app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.TransactionStep
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing

/**
 * Guided Mode specific UI components that were part of the original implementation.
 * These provide the familiar step-by-step experience for new users.
 */

/**
 * Shows completed steps in a compact checklist format.
 * Allows users to go back and edit previous steps.
 * This was a key feature of the original Guided Mode.
 */
@Composable
fun CompletedStepsChecklist(
    transactionInput: TransactionInput,
    currentStep: TransactionStep,
    onStepClicked: (TransactionStep) -> Unit,
    modifier: Modifier = Modifier
) {
    val allSteps = TransactionStep.entries.filter { it != TransactionStep.Done }

    // Filter out steps not relevant to current flow
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

/**
 * Individual completed step item with check icon and step value.
 */
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

/**
 * Returns the display value for a completed step based on current input.
 */
private fun getStepDisplayValue(step: TransactionStep, transactionInput: TransactionInput): String {
    return when (step) {
        TransactionStep.FlowSelection -> when (transactionInput.direction) {
            TransactionDirection.Inflow -> "💰 Einnahme"
            TransactionDirection.Outflow -> "💸 Ausgabe"
            TransactionDirection.AccountTransfer -> "🔄 Transfer"
            TransactionDirection.Unknown, null -> "" // Initial state - no display value
            else -> ""
        }

        TransactionStep.Amount -> transactionInput.amount?.formattedMoney ?: ""

        TransactionStep.FromAccount -> transactionInput.fromAccount?.name ?: ""

        TransactionStep.ToAccount -> transactionInput.toAccount?.name ?: ""

        TransactionStep.Partner -> transactionInput.partner.takeIf { it.isNotBlank() } ?: ""

        TransactionStep.Category -> transactionInput.category?.name ?: ""

        TransactionStep.Date -> transactionInput.date?.let { formatDate(it) } ?: ""

        TransactionStep.Optional -> if (transactionInput.description.isNotEmpty()) {
            "✏️ Beschreibung hinzugefügt"
        } else ""

        TransactionStep.Done -> "✅ Fertig"
    }
}

/**
 * Get localized step name for display.
 */
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

/**
 * Format date for display in German format.
 */
private fun formatDate(date: java.time.LocalDate): String {
    return "${date.dayOfMonth.toString().padStart(2, '0')}.${
        date.monthValue.toString().padStart(2, '0')
    }.${date.year}"
}

/**
 * Step counter component that shows current progress.
 */
@Composable
fun GuidedStepCounter(
    currentStep: TransactionStep,
    transactionInput: TransactionInput,
    modifier: Modifier = Modifier
) {
    val totalSteps = when (transactionInput.direction) {
        TransactionDirection.Inflow -> 6 // FlowSelection, Amount, FromAccount, Partner, Date, Optional
        TransactionDirection.Outflow -> 7 // FlowSelection, Amount, FromAccount, Partner, Category, Date, Optional
        TransactionDirection.AccountTransfer -> 6 // FlowSelection, Amount, FromAccount, ToAccount, Date, Optional
        else -> 8 // Default maximum
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

// ================================
// Preview Composables
// ================================

@Preview(name = "Completed Steps Checklist", showBackground = true)
@Composable
private fun CompletedStepsChecklistPreview() {
    GetALifeTheme {
        val mockTransactionInput = TransactionInput(
            direction = TransactionDirection.Outflow,
            amount = app.tinygiants.getalife.domain.model.Money(25.99),
            fromAccount = app.tinygiants.getalife.domain.model.Account(
                id = 1L,
                name = "Girokonto",
                type = app.tinygiants.getalife.domain.model.AccountType.Checking,
                balance = app.tinygiants.getalife.domain.model.Money(1000.0),
                listPosition = 0,
                createdAt = kotlin.time.Clock.System.now(),
                updatedAt = kotlin.time.Clock.System.now()
            ),
            partner = "Edeka"
        )

        CompletedStepsChecklist(
            transactionInput = mockTransactionInput,
            currentStep = TransactionStep.Category,
            onStepClicked = { },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Guided Step Counter", showBackground = true)
@Composable
private fun GuidedStepCounterPreview() {
    GetALifeTheme {
        GuidedStepCounter(
            currentStep = TransactionStep.Category,
            transactionInput = TransactionInput(
                direction = TransactionDirection.Outflow
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}