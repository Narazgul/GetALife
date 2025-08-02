package app.tinygiants.getalife.presentation.main_app.budget.composables.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = spacing.s)
                    .size(width = 32.dp, height = 4.dp)
                    .background(
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.l, vertical = spacing.s)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title
            Text(
                text = stringResource(R.string.distribute_money),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = spacing.l)
            )

            // Amount Input
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(spacing.l)
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
                    label = {
                        Text(
                            "Betrag zuweisen",
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            assignedMoneyUserInput =
                                if (focusState.isFocused) ""
                                else assignedMoneyUserInput
                        },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(spacing.m)
                )
            }

            // Bottom padding for navigation bar
            Spacer(modifier = Modifier.height(48.dp))
        }
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