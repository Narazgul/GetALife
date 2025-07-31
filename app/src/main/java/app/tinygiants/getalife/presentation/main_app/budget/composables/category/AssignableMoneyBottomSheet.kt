package app.tinygiants.getalife.presentation.main_app.budget.composables.category

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.spacing
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignableMoneyBottomSheet(
    assignedMoney: Money,
    onAssignedMoneyChanged: (Money) -> Unit = { },
    onDismissRequest: () -> Unit = { }
) {

    var assignedMoneyInCategory by remember { mutableStateOf(assignedMoney) }
    var assignedMoneyUserInput by rememberSaveable { mutableStateOf(assignedMoneyInCategory.formattedPositiveMoney) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {

        TextField(
            value = assignedMoneyUserInput,
            onValueChange = { userInput ->
                assignedMoneyUserInput = userInput.replace(oldChar = ',', newChar = '.')

                if (assignedMoneyUserInput.isEmpty()) {
                    assignedMoneyInCategory = Money(value = 0.0)
                    onAssignedMoneyChanged(assignedMoneyInCategory)
                } else {
                    val positiveOrNegativeValue = assignedMoneyUserInput.toDoubleOrNull() ?: return@TextField
                    val positiveValue = abs(positiveOrNegativeValue)
                    assignedMoneyInCategory = Money(value = positiveValue)
                    onAssignedMoneyChanged(assignedMoneyInCategory)
                }
            },
            label = { Text(stringResource(R.string.distribute_money)) },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.l)
                .onFocusChanged { focusState ->
                    assignedMoneyUserInput =
                        if (focusState.isFocused) ""
                        else assignedMoneyUserInput
                }
        )

        Spacer(modifier = Modifier.height(spacing.xl))
    }
}

@Preview
@Composable
private fun EditCategoryPreview() {
    GetALifeTheme {
        Surface {
            AssignableMoneyBottomSheet(assignedMoney = Money(100.00))
        }
    }
}