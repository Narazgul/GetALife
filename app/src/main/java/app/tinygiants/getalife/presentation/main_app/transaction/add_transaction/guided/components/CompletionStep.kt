package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.onSuccess

/**
 * Reusable completion step component for celebrating successful transactions.
 * Shows success icon and allows user to continue with standard mode.
 */
@Composable
fun CompletionStep(
    onSwitchToStandardMode: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = " Geschafft!",
    message: String = "Deine erste Transaktion wurde erfolgreich gespeichert.\nAb sofort siehst du die Schnellansicht.",
    buttonText: String = "Weitere Transaktion hinzufügen"
) {
    // Stable callback to prevent unnecessary recompositions
    val stableCallback = remember(onSwitchToStandardMode) { onSwitchToStandardMode }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ) {
        // Success icon
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = onSuccess,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.padding(vertical = 12.dp))

        // Success title
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = onSuccess,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Success message
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.padding(vertical = 16.dp))

        // Continue button
        Button(
            onClick = stableCallback,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Text(buttonText)
        }
    }
}

// Previews
@Preview(name = "Completion Step - Default", showBackground = true)
@Composable
private fun CompletionStepPreview() {
    GetALifeTheme {
        CompletionStep(
            onSwitchToStandardMode = { }
        )
    }
}

@Preview(name = "Completion Step - Custom Message", showBackground = true)
@Composable
private fun CompletionStepCustomPreview() {
    GetALifeTheme {
        CompletionStep(
            onSwitchToStandardMode = { },
            title = " Super gemacht!",
            message = "Dein Transfer wurde erfolgreich verarbeitet.\nDu kannst jetzt weitere Transaktionen hinzufügen.",
            buttonText = "Noch einen Transfer erstellen"
        )
    }
}

@Preview(name = "Completion Step - Budget Setup", showBackground = true)
@Composable
private fun CompletionStepBudgetPreview() {
    GetALifeTheme {
        CompletionStep(
            onSwitchToStandardMode = { },
            title = " Dein Budget ist bereit!",
            message = "Alle Grundeinstellungen sind abgeschlossen.\nJetzt kannst du mit der Budgetverwaltung starten.",
            buttonText = "Zum Budget-Dashboard"
        )
    }
}