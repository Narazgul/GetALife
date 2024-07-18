package app.tinygiants.getalife.presentation.budget.composables.category

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
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
    var showAssignMoneyBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showGeneralEditBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showEmojiPicker by rememberSaveable { mutableStateOf(false) }

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "animatedProgress")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    showAssignMoneyBottomSheet = true
                    showGeneralEditBottomSheet = false
                },
                onLongClick = {
                    showGeneralEditBottomSheet = true
                    showAssignMoneyBottomSheet = false
                }
            )
            .padding(
                horizontal = spacing.l,
                vertical = spacing.s
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
            Spacer(modifier = Modifier.size(spacing.s))
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
            Spacer(modifier = Modifier.size(spacing.s))
            Text(
                text = "Assigned: ${assignedMoney.formattedMoney}",
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.size(spacing.l))
            Box(
                modifier = Modifier
                    .background(
                        color = budgetTargetBackground,
                        shape = RoundedCornerShape(spacing.l)
                    )
                    .padding(
                        horizontal = spacing.default,
                        vertical = spacing.xs
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
                .height(spacing.s)
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

    if (showGeneralEditBottomSheet) EditGeneralCategoryBottomSheet(
        categoryName = categoryName,
        budgetTarget = budgetTarget,
        budgetPurpose = budgetPurpose,
        onUpdateCategoryName = onUpdateCategoryClicked,
        onBudgetTargetChanged = { money -> onUpdateBudgetTargetClicked(money) },
        onUpdateBudgetPurposeClicked = onUpdateBudgetPurposeClicked,
        onDeleteCategoryClicked = onDeleteCategoryClicked,
        onDismissRequest = { showGeneralEditBottomSheet = false },
    )

    if (showAssignMoneyBottomSheet) AssignableMoneyBottomSheet(
        assignedMoney = assignedMoney,
        onAssignedMoneyChanged = { money -> onUpdateAssignedMoneyClicked(money) },
        onDismissRequest = { showAssignMoneyBottomSheet = false }
    )

    if (showEmojiPicker) {
        val sheetSate = rememberModalBottomSheetState()
        val scope = rememberCoroutineScope()
        fun hideModalBottomSheetIfFullyExpanded() {
            if (sheetSate.currentValue == SheetValue.Expanded) {
                scope.launch { sheetSate.hide() }.invokeOnCompletion { showEmojiPicker = false }
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