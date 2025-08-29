package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.theme.GetALifeTheme

/**
 * Reusable text input component with hoisted state.
 * Can be used for partner input, description, or any text field.
 *
 * Performance optimized with stable callbacks to prevent unnecessary recompositions.
 */
@Composable
fun TextInput(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    showNextButton: Boolean = true,
    nextButtonText: String = "Weiter",
    isRequired: Boolean = true,
    maxLines: Int = 1,
    imeAction: ImeAction = ImeAction.Done
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isValid = if (isRequired) value.trim().isNotEmpty() else true

    // Stable callbacks to prevent unnecessary recompositions
    val stableNextCallback = remember(onNextClicked) { onNextClicked }
    val stableKeyboardAction = remember(keyboardController, onNextClicked, isValid) {
        { _: androidx.compose.foundation.text.KeyboardActionScope ->
            if (isValid) {
                keyboardController?.hide()
                onNextClicked()
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                if (placeholder.isNotEmpty()) {
                    Text(placeholder)
                } else {
                    Text("")
                }
            },
            singleLine = maxLines == 1,
            maxLines = maxLines,
            keyboardOptions = KeyboardOptions(imeAction = imeAction),
            keyboardActions = KeyboardActions(
                onDone = stableKeyboardAction
            ),
            modifier = if (maxLines == 1) {
                Modifier.width(280.dp)
            } else {
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            }
        )

        if (showNextButton) {
            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            Button(
                onClick = stableNextCallback,
                enabled = isValid,
                modifier = if (maxLines > 1) {
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                } else {
                    Modifier
                }
            ) {
                Text(nextButtonText)
            }
        }
    }
}

/**
 * Specialized component for partner input
 */
@Composable
fun PartnerInput(
    partner: String,
    onPartnerChange: (String) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextInput(
        title = "Mit wem war die Transaktion?",
        value = partner,
        onValueChange = onPartnerChange,
        onNextClicked = onNextClicked,
        placeholder = "z.B. Netflix, Edeka, Max Mustermann",
        modifier = modifier
    )
}

/**
 * Specialized component for description input (optional)
 */
@Composable
fun DescriptionInput(
    description: String,
    onDescriptionChange: (String) -> Unit,
    onFinishClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextInput(
        title = "Möchtest du eine Notiz hinzufügen?",
        value = description,
        onValueChange = onDescriptionChange,
        onNextClicked = onFinishClicked,
        placeholder = "Notiz (optional)",
        nextButtonText = "✨ Transaktion speichern",
        isRequired = false,
        maxLines = 3,
        imeAction = ImeAction.Done,
        modifier = modifier
    )
}

// Previews
@Preview(name = "Partner Input", showBackground = true)
@Composable
private fun PartnerInputPreview() {
    GetALifeTheme {
        PartnerInput(
            partner = "Netflix",
            onPartnerChange = { },
            onNextClicked = { }
        )
    }
}

@Preview(name = "Description Input", showBackground = true)
@Composable
private fun DescriptionInputPreview() {
    GetALifeTheme {
        DescriptionInput(
            description = "Monatliches Streaming-Abo für die Familie",
            onDescriptionChange = { },
            onFinishClicked = { }
        )
    }
}

@Preview(name = "Empty Required Input", showBackground = true)
@Composable
private fun EmptyRequiredInputPreview() {
    GetALifeTheme {
        TextInput(
            title = "Mit wem war die Transaktion?",
            value = "",
            onValueChange = { },
            onNextClicked = { },
            placeholder = "z.B. Netflix, Edeka, Max Mustermann",
            isRequired = true
        )
    }
}