package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.shared

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
 * Unified progress indicator for guided transaction flows.
 *
 * Features:
 * - Dynamic progress calculation based on transaction type
 * - Motivational progress text that adapts to current step
 * - Clean, modern design that works with wave background
 * - Consistent styling across all flows
 */
@Composable
fun TransactionProgressIndicator(
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
 * Compact version for situations where space is limited.
 */
@Composable
fun CompactTransactionProgressIndicator(
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

// ================================
// Preview Composables  
// ================================

@Preview(name = "Progress - Inflow Amount Step", showBackground = true)
@Composable
private fun TransactionProgressIndicatorInflowPreview() {
    GetALifeTheme {
        TransactionProgressIndicator(
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
private fun TransactionProgressIndicatorOutflowPreview() {
    GetALifeTheme {
        TransactionProgressIndicator(
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
private fun TransactionProgressIndicatorTransferPreview() {
    GetALifeTheme {
        TransactionProgressIndicator(
            currentStep = TransactionStep.ToAccount,
            transactionInput = TransactionInput(
                direction = TransactionDirection.AccountTransfer,
                amount = Money(100.0)
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Compact Progress Indicator", showBackground = true)
@Composable
private fun CompactTransactionProgressIndicatorPreview() {
    GetALifeTheme {
        CompactTransactionProgressIndicator(
            currentStep = TransactionStep.Date,
            transactionInput = TransactionInput(
                direction = TransactionDirection.Outflow
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Progress - Almost Complete", showBackground = true)
@Composable
private fun TransactionProgressIndicatorAlmostCompletePreview() {
    GetALifeTheme {
        TransactionProgressIndicator(
            currentStep = TransactionStep.Optional,
            transactionInput = TransactionInput(
                direction = TransactionDirection.Outflow,
                amount = Money(42.99)
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}