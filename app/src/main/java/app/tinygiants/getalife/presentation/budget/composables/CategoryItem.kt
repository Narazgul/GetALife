package app.tinygiants.getalife.presentation.budget.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.theme.ComponentPreview
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.onSuccess
import app.tinygiants.getalife.theme.onWarning
import app.tinygiants.getalife.theme.spacing
import app.tinygiants.getalife.theme.success
import app.tinygiants.getalife.theme.warning

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun Category(
    name: String = "",
    budgetTarget: Money = Money(value = 0.0),
    availableMoney: Money = Money(value = 0.0),
    progress: Float = 0f,
    optionalText: String? = null,
    onUpdateCategoryClicked: (String) -> Unit = { },
    onUpdateBudgetTargetClicked: (Money) -> Unit = { },
    onUpdateAvailableMoneyClicked: (Money) -> Unit = { },
    onDeleteCategoryClicked: () -> Unit = { }
) {

    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    var categoryNameUserInput by rememberSaveable { mutableStateOf(name) }
    var budget by remember { mutableStateOf(budgetTarget) }
    var money by remember { mutableStateOf(availableMoney) }
    var budgetTargetUserInput by rememberSaveable { mutableStateOf(budget.value.toString()) }
    var availableMoneyUserInput by rememberSaveable { mutableStateOf(money.value.toString()) }

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "animatedProgress")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = { showBottomSheet = true }
            )
            .padding(
                horizontal = spacing.large,
                vertical = spacing.default
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            val budgetTargetBackground = when {
                availableMoney.value == 0.0 -> MaterialTheme.colorScheme.outlineVariant
                availableMoney.value < budgetTarget.value -> onWarning
                else -> onSuccess
            }
            Box(
                modifier = Modifier
                    .background(
                        color = budgetTargetBackground,
                        shape = RoundedCornerShape(spacing.large)
                    )
                    .padding(
                        horizontal = spacing.default,
                        vertical = spacing.extraSmall
                    )
            ) {
                val formattedBudgetTarget = availableMoney.formattedMoney
                val availableMoneyColor = when {
                    availableMoney.value > 0.00 -> MaterialTheme.colorScheme.scrim
                    else -> MaterialTheme.colorScheme.onSurface
                }

                Text(
                    text = formattedBudgetTarget,
                    style = MaterialTheme.typography.titleMedium,
                    color = availableMoneyColor
                )
            }
        }
        Spacer(modifier = Modifier.height(spacing.default))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(spacing.small)
        ) {
            val progressBackground = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            val progressColor = when {
                availableMoney.value == 0.0 -> MaterialTheme.colorScheme.primary
                availableMoney.value < budgetTarget.value -> warning
                else -> success
            }
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth(),
                color = progressColor,
                trackColor = progressBackground,
                strokeCap = StrokeCap.Round,
            )
        }
        Spacer(modifier = Modifier.height(spacing.default))
        if (!optionalText.isNullOrBlank()) {
            Text(
                text = optionalText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Start
            )
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(
                modifier = Modifier.padding(horizontal = spacing.large)
            ) {
                Row {
                    TextField(
                        value = categoryNameUserInput,
                        onValueChange = { userInput -> categoryNameUserInput = userInput },
                        label = { Text("Kategorie umbenennen") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(spacing.default))
                    Button(
                        onClick = {
                            if (categoryNameUserInput.isNotBlank()) {
                                onUpdateCategoryClicked(categoryNameUserInput)
                                categoryNameUserInput = ""
                            }
                        }
                    ) { Text(text = "Speichern") }
                }
                Spacer(modifier = Modifier.height(spacing.default))
                Row {
                    TextField(
                        value = budgetTargetUserInput,
                        onValueChange = { userInput ->
                            budgetTargetUserInput = userInput
                            budget = Money(userInput.toDoubleOrNull() ?: budget.value)
                        },
                        prefix = { Text(availableMoney.currencySymbol) },
                        label = { Text("Budget Ziel ändern") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState ->
                                budgetTargetUserInput = if (focusState.hasFocus) "" else budget.value.toString()
                            }
                    )
                    Spacer(modifier = Modifier.width(spacing.default))
                    Button(onClick = { onUpdateBudgetTargetClicked(Money(value = budget.value)) }) { Text(text = "Speichern") }
                }
                Spacer(modifier = Modifier.height(spacing.default))
                Row {
                    TextField(
                        value = availableMoneyUserInput,
                        onValueChange = { userInput ->
                            availableMoneyUserInput = userInput
                            money = Money(userInput.toDoubleOrNull() ?: money.value)
                        },
                        prefix = { Text(availableMoney.currencySymbol) },
                        label = { Text("Verfügbares Geld ändern") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState ->
                                availableMoneyUserInput = if (focusState.hasFocus) "" else money.value.toString()
                            }
                    )
                    Spacer(modifier = Modifier.width(spacing.default))
                    Button(onClick = { onUpdateAvailableMoneyClicked(Money(value = money.value)) }) { Text(text = "Speichern") }
                }
                Spacer(modifier = Modifier.height(spacing.default))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { onDeleteCategoryClicked() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(
                            text = "$name löschen",
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(spacing.extraLarge))
        }
    }
}

@ComponentPreview
@Composable
fun FullCategoryPreview() {
    GetALifeTheme {
        Surface {
            Category(
                name = "Rent",
                budgetTarget = Money(940.00),
                availableMoney = Money(940.00),
                progress = 1f,
                optionalText = optionalExampleText(0.00)
            )
        }
    }
}

@ComponentPreview
@Composable
fun SemiFilledCategoryPreview() {
    GetALifeTheme {
        Surface {
            Category(
                name = "Rent",
                budgetTarget = Money(940.00),
                availableMoney = Money(470.00),
                progress = (470.00 / 940.00).toFloat(),
                optionalText = optionalExampleText(gap = 940.00 - 470.00)
            )
        }
    }
}

@ComponentPreview
@Composable
fun EmptyCategoryPreview() {
    GetALifeTheme {
        Surface {
            Category(
                name = "Rent",
                budgetTarget = Money(940.00),
                availableMoney = Money(0.0),
                progress = (0.0 / 940.00).toFloat(),
                optionalText = optionalExampleText(gap = 940.00 - 0.00)
            )
        }
    }
}

fun optionalExampleText(gap: Double) = "$gap more needed by the 30th"