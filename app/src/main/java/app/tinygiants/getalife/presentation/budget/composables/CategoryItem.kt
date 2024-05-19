package app.tinygiants.getalife.presentation.budget.composables

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.emoji2.emojipicker.EmojiPickerView
import app.tinygiants.getalife.domain.model.BudgetPurpose
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.onSuccess
import app.tinygiants.getalife.theme.onWarning
import app.tinygiants.getalife.theme.spacing
import app.tinygiants.getalife.theme.success
import app.tinygiants.getalife.theme.warning
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Category(
    emoji: String = "",
    categoryName: String = "",
    budgetTarget: Money = Money(value = 0.0),
    budgetPurpose: BudgetPurpose = BudgetPurpose.Unknown,
    assignedMoney: Money = Money(value = 0.0),
    availableMoney: Money = Money(value = 0.0),
    progress: Float = 0f,
    optionalText: String? = null,
    onUpdateEmojiClicked: (String) -> Unit = { },
    onUpdateCategoryClicked: (String) -> Unit = { },
    onUpdateBudgetTargetClicked: (Money) -> Unit = { },
    onUpdateBudgetPurposeClicked: (BudgetPurpose) -> Unit = { },
    onUpdateAssignedMoneyClicked: (Money) -> Unit = { },
    onDeleteCategoryClicked: () -> Unit = { }
) {
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    //TODO implement TransactionSheet
    var showTransactionSheet by rememberSaveable { mutableStateOf(false) }
    var showEmojiPicker by rememberSaveable { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "animatedProgress")

    var budgetTargetMoney by remember { mutableStateOf(budgetTarget) }
    var assignedMoneyToCategory by remember { mutableStateOf(assignedMoney) }

    var budgetTargetUserInput by rememberSaveable { mutableStateOf(budgetTargetMoney.value.toString()) }
    var assignedMoneyUserInput by rememberSaveable { mutableStateOf(assignedMoneyToCategory.value.toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { showBottomSheet = true },
                onLongClick = { showTransactionSheet = true }
            )
            .padding(
                horizontal = spacing.large,
                vertical = spacing.small
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                maxLines = 1,
                modifier = Modifier
                    .clickable(onClick = { showEmojiPicker = true })
                    .widthIn(max = 20.dp)
            )
            Spacer(modifier = Modifier.size(spacing.small))
            Text(
                text = categoryName,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.weight(1f)
            )
            val budgetTargetBackground = when {
                availableMoney.value == 0.0 -> MaterialTheme.colorScheme.outlineVariant
                availableMoney.value < budgetTarget.value -> onWarning
                else -> onSuccess
            }
            Spacer(modifier = Modifier.size(spacing.small))
            Text(
                text = "Assigned: ${assignedMoneyToCategory.formattedMoney}",
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.size(spacing.large))
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    MultiChoiceSegmentedButtonRow {
                        SegmentedButton(
                            checked = budgetPurpose == BudgetPurpose.Spending,
                            onCheckedChange = { isChecked ->
                                if (isChecked) onUpdateBudgetPurposeClicked(BudgetPurpose.Spending)
                                else onUpdateBudgetPurposeClicked(BudgetPurpose.Unknown)
                            },
                            shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                        ) {
                            Text(text = "Ausgeben")
                        }
                        SegmentedButton(
                            checked = budgetPurpose == BudgetPurpose.Saving,
                            onCheckedChange = { isChecked ->
                                if (isChecked) onUpdateBudgetPurposeClicked(BudgetPurpose.Saving)
                                else onUpdateBudgetPurposeClicked(BudgetPurpose.Unknown)
                            },
                            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                        ) {
                            Text(text = "Sparen")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(spacing.default))
                Row {
                    TextField(
                        value = categoryName,
                        onValueChange = { userInput -> onUpdateCategoryClicked(userInput) },
                        label = { Text("Kategorie umbenennen") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(spacing.default))
                Row {
                    TextField(
                        value = budgetTargetUserInput,
                        onValueChange = { userInput ->
                            budgetTargetUserInput = userInput
                            budgetTargetMoney = Money(userInput.toDoubleOrNull() ?: budgetTargetMoney.value)
                            onUpdateBudgetTargetClicked(budgetTargetMoney)
                        },
                        prefix = { Text(budgetTargetMoney.currencySymbol) },
                        label = { Text("Budget Ziel ändern") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState ->
                                budgetTargetUserInput =
                                    if (focusState.hasFocus) "" else budgetTargetMoney.value.toString()
                            }
                    )
                }
                Spacer(modifier = Modifier.height(spacing.default))
                Row {
                    TextField(
                        value = assignedMoneyUserInput,
                        onValueChange = { userInput ->
                            assignedMoneyUserInput = userInput
                            assignedMoneyToCategory = Money(userInput.toDoubleOrNull() ?: 0.00)
                            onUpdateAssignedMoneyClicked(assignedMoneyToCategory)
                        },
                        prefix = { Text(assignedMoney.currencySymbol) },
                        label = { Text("Geld zuweisen") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState ->
                                assignedMoneyUserInput =
                                    if (focusState.hasFocus) "" else assignedMoney.value.toString()
                            }
                    )
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
                            text = "$categoryName löschen",
                            color = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(spacing.extraLarge))
        }
    }

    if (showEmojiPicker) {
        val sheetSate = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()
        fun hideModalBottomSheetIfFullyExpanded() {
            if (sheetSate.currentValue == SheetValue.Expanded) {
                scope.launch { sheetSate.hide() }.invokeOnCompletion { showBottomSheet = false }
            }
        }

        ModalBottomSheet(
            onDismissRequest = { showEmojiPicker = false },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            sheetState = sheetSate
        ) {
            AndroidView(
                modifier = Modifier.fillMaxWidth(),
                factory = { context ->
                    EmojiPickerView(context).apply {
                        setOnEmojiPickedListener { emojiViewItem ->
                            onUpdateEmojiClicked(emojiViewItem.emoji)
                            hideModalBottomSheetIfFullyExpanded()
                        }
                    }
                }
            )
        }
    }
}


@PreviewLightDark
@Composable
fun FullCategoryPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(940.00),
                assignedMoney = Money(940.00),
                availableMoney = Money(940.00),
                progress = 1f,
                optionalText = optionalExampleText(0.00)
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SemiFilledCategoryPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(940.00),
                assignedMoney = Money(470.00),
                availableMoney = Money(470.00),
                progress = (470.00 / 940.00).toFloat(),
                optionalText = optionalExampleText(gap = 940.00 - 470.00)
            )
        }
    }
}

@PreviewLightDark
@Composable
fun EmptyCategoryPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(940.00),
                assignedMoney = Money(0.0),
                availableMoney = Money(0.0),
                progress = (0.0 / 940.00).toFloat(),
                optionalText = optionalExampleText(gap = 940.00 - 0.00)
            )
        }
    }
}

fun optionalExampleText(gap: Double) = "$gap more needed by the 30th"