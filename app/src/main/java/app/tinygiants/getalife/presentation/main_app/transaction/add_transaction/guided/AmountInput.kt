package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.presentation.shared_composables.InputValidationUtils
import app.tinygiants.getalife.theme.GetALifeTheme
import kotlinx.coroutines.delay

/**
 * Reusable amount input component with auto-focus and validation.
 * Features large display text and euro symbol.
 */
@Composable
fun AmountInput(
    amountText: String,
    onAmountTextChanged: (String) -> Unit,
    onAmountChanged: (Money) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Gib den Betrag ein (in Euro)",
    showNextButton: Boolean = true,
    autoFocus: Boolean = true
) {
    val isValid = InputValidationUtils.isValidAmountInput(amountText)
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // Stable callbacks to prevent unnecessary recompositions
    val stableAmountChangeCallback = remember(onAmountChanged) {
        { textValue: String ->
            val amount = InputValidationUtils.parseAmountInput(textValue)
            onAmountChanged(amount)
        }
    }
    val stableNextCallback = remember(onNextClicked) { onNextClicked }
    val stableKeyboardAction = remember(keyboardController, onNextClicked, isValid) {
        { _: KeyboardActionScope ->
            if (isValid && showNextButton) {
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

        BasicTextField(
            value = amountText,
            onValueChange = { newValue ->
                onAmountTextChanged(newValue)
                stableAmountChangeCallback(newValue)
            },
            modifier = Modifier
                .width(320.dp)
                .padding(vertical = 24.dp)
                .focusRequester(focusRequester),
            textStyle = MaterialTheme.typography.displayLarge.copy(
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = if (showNextButton) ImeAction.Next else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onNext = stableKeyboardAction,
                onDone = stableKeyboardAction
            ),
            singleLine = true,
            decorationBox = { innerTextField ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (amountText.isEmpty()) {
                        Text(
                            text = "0,00",
                            style = MaterialTheme.typography.displayLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        innerTextField()
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "€",
                            style = MaterialTheme.typography.displayLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        )

        if (showNextButton) {
            Spacer(Modifier.height(24.dp))

            Button(
                onClick = stableNextCallback,
                enabled = isValid
            ) {
                Text("Weiter")
            }
        }
    }

    // Auto-focus when component appears
    if (autoFocus) {
        LaunchedEffect(Unit) {
            delay(100) // Small delay to ensure UI is ready
            focusRequester.requestFocus()
        }
    }
}

// Preview
@Preview(name = "Amount Input - Empty", showBackground = true)
@Composable
private fun AmountInputEmptyPreview() {
    GetALifeTheme {
        AmountInput(
            amountText = "",
            onAmountTextChanged = { },
            onAmountChanged = { },
            onNextClicked = { },
            showNextButton = true,
            autoFocus = false
        )
    }
}

@Preview(name = "Amount Input - With Value", showBackground = true)
@Composable
private fun AmountInputWithValuePreview() {
    GetALifeTheme {
        AmountInput(
            amountText = "25.99",
            onAmountTextChanged = { },
            onAmountChanged = { },
            onNextClicked = { },
            showNextButton = true,
            autoFocus = false
        )
    }
}

@Preview(name = "Amount Input - No Button", showBackground = true)
@Composable
private fun AmountInputNoButtonPreview() {
    GetALifeTheme {
        AmountInput(
            amountText = "",
            onAmountTextChanged = { },
            onAmountChanged = { },
            onNextClicked = { },
            showNextButton = false,
            title = "Wie viel Geld möchtest du transferieren?",
            autoFocus = false
        )
    }
}