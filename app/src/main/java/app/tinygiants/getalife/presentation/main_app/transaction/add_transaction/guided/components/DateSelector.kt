package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction.guided.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.tinygiants.getalife.theme.GetALifeTheme
import java.time.LocalDate

/**
 * Reusable date selection component with hoisted state.
 * Shows localized date formatting and DatePicker dialog.
 *
 * Performance optimized with stable callbacks to prevent unnecessary recompositions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelector(
    title: String,
    selectedDate: LocalDate?,
    showDatePicker: Boolean,
    onDateSelected: (LocalDate) -> Unit,
    onShowDatePickerChanged: (Boolean) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier,
    showNextButton: Boolean = true,
    nextButtonText: String = "Weiter"
) {
    // Stable callbacks to prevent unnecessary recompositions
    val stableShowPickerCallback = remember(onShowDatePickerChanged) {
        { onShowDatePickerChanged(true) }
    }
    val stableHidePickerCallback = remember(onShowDatePickerChanged) {
        { onShowDatePickerChanged(false) }
    }
    val stableNextCallback = remember(onNextClicked) { onNextClicked }

    // DatePicker uses milliseconds since epoch
    val initialMillis = selectedDate?.atStartOfDay(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
        ?: System.currentTimeMillis()

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

    // Convert millis back to LocalDate
    val pickedDate = datePickerState.selectedDateMillis?.let { millis ->
        java.time.Instant.ofEpochMilli(millis)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()
    }

    // Date formatter for display
    val dateFormatter = remember {
        java.time.format.DateTimeFormatter.ofLocalizedDate(java.time.format.FormatStyle.MEDIUM)
            .withLocale(java.util.Locale.getDefault())
    }

    val isValid = pickedDate != null

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

        // Date display button
        TextButton(
            onClick = stableShowPickerCallback,
            modifier = Modifier.width(240.dp)
        ) {
            val dateStr = pickedDate?.let { dateFormatter.format(it) } ?: "Datum wählen"
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        if (showNextButton) {
            Spacer(modifier = Modifier.padding(vertical = 12.dp))

            Button(
                onClick = {
                    pickedDate?.let { date ->
                        onDateSelected(date)
                        stableNextCallback()
                    }
                },
                enabled = isValid
            ) {
                Text(nextButtonText)
            }
        }
    }

    // Show DatePickerDialog if toggled on
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = stableHidePickerCallback,
            confirmButton = {
                TextButton(
                    onClick = {
                        pickedDate?.let { date ->
                            onDateSelected(date)
                            stableHidePickerCallback()
                        }
                    },
                    enabled = isValid
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = stableHidePickerCallback) {
                    Text("Abbrechen")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// Previews
@Preview(name = "Date Selector - Default", showBackground = true)
@Composable
private fun DateSelectorPreview() {
    GetALifeTheme {
        DateSelector(
            title = "Wann war die Transaktion?",
            selectedDate = LocalDate.now(),
            showDatePicker = false,
            onDateSelected = { },
            onShowDatePickerChanged = { },
            onNextClicked = { }
        )
    }
}

@Preview(name = "Date Selector - No Selection", showBackground = true)
@Composable
private fun DateSelectorNoSelectionPreview() {
    GetALifeTheme {
        DateSelector(
            title = "Wann war die Transaktion?",
            selectedDate = null,
            showDatePicker = false,
            onDateSelected = { },
            onShowDatePickerChanged = { },
            onNextClicked = { }
        )
    }
}

@Preview(name = "Date Selector - No Next Button", showBackground = true)
@Composable
private fun DateSelectorNoButtonPreview() {
    GetALifeTheme {
        DateSelector(
            title = "Transaktionsdatum wählen",
            selectedDate = LocalDate.now().minusDays(3),
            showDatePicker = false,
            onDateSelected = { },
            onShowDatePickerChanged = { },
            onNextClicked = { },
            showNextButton = false
        )
    }
}