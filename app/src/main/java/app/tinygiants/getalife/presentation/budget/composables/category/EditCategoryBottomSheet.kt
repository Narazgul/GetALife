package app.tinygiants.getalife.presentation.budget.composables.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCategoryBottomSheet(
    categoryName: String,
    budgetTarget: Money,
    onUpdateCategoryName: (String) -> Unit = { },
    onBudgetTargetChanged: (Money) -> Unit = { },
    onDeleteCategoryClicked: () -> Unit = { },
    onDismissRequest: () -> Unit = { }
) {

    var categoryNameUserInput by rememberSaveable { mutableStateOf(categoryName) }

    var budgetTargetMoney by remember { mutableStateOf(budgetTarget) }
    var budgetTargetUserInput by rememberSaveable { mutableStateOf(budgetTargetMoney.value.toString()) }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(modifier = Modifier.padding(horizontal = spacing.l)) {
            Spacer(modifier = Modifier.height(spacing.default))
            Row {
                TextField(
                    value = categoryNameUserInput,
                    onValueChange = { userInput ->
                        categoryNameUserInput = userInput
                        onUpdateCategoryName(userInput)
                    },
                    label = { Text(stringResource(R.string.rename_category)) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(spacing.default))
            Row {
                TextField(
                    value = budgetTargetUserInput,
                    onValueChange = { userInput ->
                        budgetTargetUserInput = userInput.replace(oldChar = ',', newChar = '.')
                        budgetTargetMoney =
                            Money(value = budgetTargetUserInput.toDoubleOrNull() ?: return@TextField)
                        onBudgetTargetChanged(budgetTargetMoney)
                    },
                    prefix = { Text(budgetTarget.currencySymbol) },
                    label = { Text(stringResource(R.string.change_budget_target)) },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focusState ->
                            budgetTargetUserInput = if (focusState.hasFocus) "" else budgetTarget.value.toString()
                        }
                )
            }
            Spacer(modifier = Modifier.height(spacing.default))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onDeleteCategoryClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(
                        text = stringResource(R.string.delete_transaction, categoryName),
                        color = MaterialTheme.colorScheme.onError
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(spacing.xl))
    }
}