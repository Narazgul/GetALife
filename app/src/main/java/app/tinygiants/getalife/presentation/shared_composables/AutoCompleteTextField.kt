package app.tinygiants.getalife.presentation.shared_composables

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import app.tinygiants.getalife.theme.GetALifeTheme

/**
 * Reusable TextField with simple dropdown‐based auto-complete.
 * @param value aktueller Textwert
 * @param onValueChange Callback bei Eingabeänderung
 * @param suggestions Liste möglicher Vorschläge (z. B. letzte TransactionPartner)
 * @param label Beschriftung des Textfeldes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AutoCompleteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    label: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        TextField(
            value = value,
            onValueChange = { input ->
                onValueChange(input)
                expanded = input.isNotBlank()
            },
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
            )
        )

        DropdownMenu(
            expanded = expanded && suggestions.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            suggestions
                .filter { it.contains(value, ignoreCase = true) }
                .take(10)
                .forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion, fontWeight = FontWeight.Medium) },
                        onClick = {
                            onValueChange(suggestion)
                            expanded = false
                        }
                    )
                }
        }
    }
}

@Preview
@Composable
private fun AutoCompletePreview() {
    GetALifeTheme {
        AutoCompleteTextField(
            value = "Re",
            onValueChange = {},
            suggestions = listOf("REWE", "REWE Center", "Real", "Rossmann"),
            label = "Partner"
        )
    }
}