package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.tinygiants.getalife.BuildConfig
import app.tinygiants.getalife.theme.spacing

@Composable
fun DebugModeToggle(
    isGuidedMode: Boolean,
    onToggleMode: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!BuildConfig.DEBUG) return

    Button(
        onClick = onToggleMode,
        modifier = modifier
            .fillMaxWidth()
            .padding(spacing.m),
        colors = ButtonDefaults.outlinedButtonColors(),
        shape = RoundedCornerShape(spacing.s)
    ) {
        Text(
            text = if (isGuidedMode) {
                "🔧 DEBUG: Switch to Standard Mode"
            } else {
                "🔧 DEBUG: Switch to Guided Mode"
            },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}