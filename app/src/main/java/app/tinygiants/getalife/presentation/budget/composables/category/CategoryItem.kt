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
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.emoji2.emojipicker.EmojiPickerView
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.domain.model.Progress
import app.tinygiants.getalife.domain.model.ProgressColor
import app.tinygiants.getalife.domain.model.UserHint
import app.tinygiants.getalife.presentation.shared_composables.UiText
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.onSuccess
import app.tinygiants.getalife.theme.onWarning
import app.tinygiants.getalife.theme.spacing
import app.tinygiants.getalife.theme.success
import app.tinygiants.getalife.theme.warning
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Category(
    emoji: String = "",
    categoryName: String = "",
    budgetTarget: Money = Money(value = 0.0),
    assignedMoney: Money = Money(value = 0.0),
    availableMoney: Money = Money(value = 0.0),
    progress: Progress = EmptyProgress(),
    optionalText: UiText = UiText.DynamicString(value = ""),
    onUpdateEmojiClicked: (String) -> Unit = { },
    onUpdateCategoryClicked: (String) -> Unit = { },
    onUpdateBudgetTargetClicked: (Money) -> Unit = { },
    onUpdateAssignedMoneyClicked: (Money) -> Unit = { },
    onDeleteCategoryClicked: () -> Unit = { }
) {
    var showAssignMoneyBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showGeneralEditBottomSheet by rememberSaveable { mutableStateOf(false) }
    var showEmojiPicker by rememberSaveable { mutableStateOf(false) }

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
        TextRowScaffold(
            emoji = { Emoji(emoji = emoji, onClick = { showEmojiPicker = true }) },
            categoryName = { CategoryName(categoryName = categoryName) },
            availableMoney = {
                AvailableMoney(
                    availableMoney = availableMoney,
                    budgetTarget = budgetTarget,
                    assignedMoney = assignedMoney
                )
            }
        )
        Spacer(modifier = Modifier.height(spacing.xs))
        CategoryProgress(progress = progress)
        Spacer(modifier = Modifier.height(spacing.m))
        OptionalText(optionalText = optionalText)
    }

    if (showGeneralEditBottomSheet) EditCategoryBottomSheet(
        categoryName = categoryName,
        budgetTarget = budgetTarget,
        onUpdateCategoryName = onUpdateCategoryClicked,
        onBudgetTargetChanged = { money -> onUpdateBudgetTargetClicked(money) },
        onDeleteCategoryClicked = onDeleteCategoryClicked,
        onDismissRequest = { showGeneralEditBottomSheet = false },
    )

    if (showAssignMoneyBottomSheet) AssignableMoneyBottomSheet(
        assignedMoney = assignedMoney,
        onAssignedMoneyChanged = { money -> onUpdateAssignedMoneyClicked(money) },
        onDismissRequest = { showAssignMoneyBottomSheet = false }
    )

    if (showEmojiPicker) {
        EmojiPicker(
            hideEmojiPicker = { showEmojiPicker = false },
            onUpdateEmojiClicked = onUpdateEmojiClicked
        )
    }
}

@Composable
fun TextRowScaffold(
    emoji: @Composable () -> Unit,
    categoryName: @Composable RowScope.() -> Unit,
    availableMoney: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        emoji()
        Spacer(modifier = Modifier.size(spacing.s))
        categoryName()
        Spacer(modifier = Modifier.size(spacing.s))
        availableMoney()
        Spacer(modifier = Modifier.padding(bottom = spacing.xl))
    }
}

@Composable
fun Emoji(emoji: String, onClick: () -> Unit) {
    Text(
        text = emoji,
        maxLines = 1,
        modifier = Modifier
            .clickable(onClick = onClick)
            .widthIn(max = 20.dp)
    )
}

@Composable
fun RowScope.CategoryName(
    categoryName: String,
    modifier: Modifier = Modifier,
    weight: Float = 1.5f,
    maxLines: Int = 1
) {
    Text(
        text = categoryName,
        style = MaterialTheme.typography.titleSmall,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.weight(weight)
    )
}

@Composable
fun AvailableMoney(availableMoney: Money, budgetTarget: Money, assignedMoney: Money) {
    val budgetTargetBackground = when {
        availableMoney < EmptyMoney() -> MaterialTheme.colorScheme.error
        availableMoney == EmptyMoney() -> MaterialTheme.colorScheme.outlineVariant
        budgetTarget != EmptyMoney() && assignedMoney < budgetTarget -> onWarning
        else -> onSuccess
    }
    val availableMoneyColor = when {
        availableMoney < EmptyMoney() -> MaterialTheme.colorScheme.onError
        availableMoney == EmptyMoney() -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.scrim
    }

    Box(
        modifier = Modifier
            .background(
                color = budgetTargetBackground,
                shape = RoundedCornerShape(spacing.l)
            )
            .padding(
                horizontal = spacing.m,
                vertical = spacing.xs
            )
    ) {
        Text(
            text = availableMoney.formattedMoney,
            style = MaterialTheme.typography.titleMedium,
            color = availableMoneyColor
        )
    }
}

@Composable
fun CategoryProgress(progress: Progress) {
    val animateBar1 by animateFloatAsState(targetValue = progress.bar1, label = "bar1")
    val animateBar1Lite by animateFloatAsState(targetValue = progress.bar1Lite, label = "bar1Lite")
    val animateBar2 by animateFloatAsState(targetValue = progress.bar2, label = "bar2")
    val animateBar2Lite by animateFloatAsState(targetValue = progress.bar2Lite, label = "bar2Lite")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(spacing.m)
    ) {
        val progressBackground = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        val bar1Color = getComposableColor(progressColor = progress.bar1Color)
        val bar1LiteColor = getComposableColor(progressColor = progress.bar1LiteColor)
        val bar2Color = getComposableColor(progressColor = progress.bar2Color)
        val bar2LiteColor = getComposableColor(progressColor = progress.bar2LiteColor)

        LinearProgressIndicator(
            progress = { animateBar1 },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            color = bar1Color,
            trackColor = progressBackground,
            strokeCap = StrokeCap.Round
        )
        LinearProgressIndicator(
            progress = { animateBar1Lite },
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            color = bar1LiteColor,
            trackColor = Color.Transparent,
            strokeCap = StrokeCap.Round,
            drawStopIndicator = { }
        )
        if (progress.showColorOnSecondBar) {
            LinearProgressIndicator(
                progress = { animateBar2 },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                color = Color.Transparent,
                trackColor = bar2Color,
                strokeCap = StrokeCap.Round,
                drawStopIndicator = { }
            )
            LinearProgressIndicator(
                progress = { animateBar2Lite },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                color = Color.Transparent,
                trackColor = bar2LiteColor,
                strokeCap = StrokeCap.Round,
                drawStopIndicator = { }
            )
        }
    }
}

@Composable
fun OptionalText(optionalText: UiText) {
    if (optionalText.asString().isNotBlank()) {
        Text(
            text = optionalText.asString(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Start
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPicker(
    hideEmojiPicker: () -> Unit,
    onUpdateEmojiClicked: (String) -> Unit
) {
    val sheetSate = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    fun hideModalBottomSheetIfFullyExpanded() {
        if (sheetSate.currentValue == SheetValue.Expanded) {
            scope.launch { sheetSate.hide() }.invokeOnCompletion { hideEmojiPicker() }
        }
    }

    ModalBottomSheet(
        onDismissRequest = { hideEmojiPicker() },
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

@Composable
fun getComposableColor(progressColor: ProgressColor): Color = when (progressColor) {
    ProgressColor.Grey -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
    ProgressColor.Red -> MaterialTheme.colorScheme.error
    ProgressColor.Yellow -> warning
    ProgressColor.YellowLite -> onWarning
    ProgressColor.Green -> success
    ProgressColor.GreenLite -> onSuccess
    ProgressColor.Primary -> MaterialTheme.colorScheme.primary
    ProgressColor.PrimaryLite -> MaterialTheme.colorScheme.inversePrimary
    else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
}

@PreviewLightDark
@Composable
fun SetTargetAssignedBeyondTargetOverspentBeyondTargetPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "sehr langer Text der umgebrochen werden muss",
                budgetTarget = Money(100.00),
                assignedMoney = Money(120.00),
                availableMoney = Money(-20.00),
                progress = Progress(
                    bar1 = (100.0 / 140.0).toFloat(),
                    bar2 = (100.0 / 140.0).toFloat(),
                    bar2Lite = (120.0 / 140.0).toFloat(),
                    bar1Color = ProgressColor.GreenLite,
                    bar2Color = ProgressColor.PrimaryLite,
                    bar2LiteColor = ProgressColor.Red,
                    showColorOnSecondBar = true,
                    userHint = UserHint.AssignMoreOrRemoveSpending(amount = "20,-€")
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SetTargetAllAssignedOverspentBeyondTargetPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(100.00),
                assignedMoney = Money(100.00),
                availableMoney = Money(-20.00),
                progress = Progress(
                    bar1 = (100.0 / 120.0).toFloat(),
                    bar2 = (100.0 / 120.0).toFloat(),
                    bar2Lite = (100.0 / 120.0).toFloat(),
                    bar1Color = ProgressColor.GreenLite,
                    bar2Color = ProgressColor.Red,
                    showColorOnSecondBar = true,
                    userHint = UserHint.AssignMoreOrRemoveSpending(amount = "20,-€")
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SetTargetSomeAssignedOverspentBeyondTargetPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(100.00),
                assignedMoney = Money(40.00),
                availableMoney = Money(-80.00),
                progress = Progress(
                    bar1 = (100.0 / 120.0).toFloat(),
                    bar1Lite = (40.0 / 120.0).toFloat(),
                    bar2 = (100.0 / 120.0).toFloat(),
                    bar2Lite = (100.0 / 120.0).toFloat(),
                    bar1Color = ProgressColor.Red,
                    bar1LiteColor = ProgressColor.YellowLite,
                    bar2Color = ProgressColor.Red,
                    showColorOnSecondBar = true,
                    userHint = UserHint.AssignMoreOrRemoveSpending(amount = "20,-€")
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SetTargetSomeAssignedOverspentBelowTargetPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(100.00),
                assignedMoney = Money(40.00),
                availableMoney = Money(-20.00),
                progress = Progress(
                    bar1 = ((40.0 + 20.0) / 100.0).toFloat(),
                    bar1Lite = (40.0 / 100.0).toFloat(),
                    bar1Color = ProgressColor.Red,
                    bar1LiteColor = ProgressColor.YellowLite,
                    userHint = UserHint.AssignMoreOrRemoveSpending(amount = "20,-€")
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SetTargetNothingAssignedOverspentPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(100.00),
                assignedMoney = Money(0.00),
                availableMoney = Money(-20.00),
                progress = Progress(
                    bar1 = 1f,
                    bar1Color = ProgressColor.Red,
                    userHint = UserHint.AssignMoreOrRemoveSpending(amount = "20,-€")
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SetTargetOverBudgetAssignedAllSpentOverBudgetPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(100.00),
                assignedMoney = Money(120.00),
                availableMoney = Money(0.00),
                progress = Progress(
                    bar1 = (100.0 / 120.0).toFloat(),
                    bar1Lite = (100.0 / 120.0).toFloat(),
                    bar1Color = ProgressColor.Green,
                    bar1LiteColor = ProgressColor.GreenLite,
                    bar2 = (100.0 / 120.0).toFloat(),
                    bar2Lite = (100.0 / 120.0).toFloat(),
                    showColorOnSecondBar = true,
                    bar2Color = ProgressColor.PrimaryLite,
                    userHint = UserHint.NoHint
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SetTargetOverBudgetAssignedLittleSpentOverBudgetPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(100.00),
                assignedMoney = Money(120.00),
                availableMoney = Money(10.00),
                progress = Progress(
                    bar1 = (100.0 / 120.0).toFloat(),
                    bar1Lite = (100.0 / 120.0).toFloat(),
                    bar1Color = ProgressColor.Green,
                    bar1LiteColor = ProgressColor.GreenLite,
                    bar2 = (100.0 / 120.0).toFloat(),
                    bar2Lite = (110.0 / 120.0).toFloat(),
                    showColorOnSecondBar = true,
                    bar2Color = ProgressColor.PrimaryLite,
                    bar2LiteColor = ProgressColor.Primary,
                    userHint = UserHint.NoHint
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SecondPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(200.00),
                assignedMoney = Money(460.00),
                availableMoney = Money(10.00),
                progress = Progress(
                    bar1 = (200.0 / 460.0).toFloat(),
                    bar1Lite = (200.0 / 460.0).toFloat(),
                    bar1Color = ProgressColor.Green,
                    bar1LiteColor = ProgressColor.GreenLite,
                    bar2 = (200.0 / 460.0).toFloat(),
                    bar2Lite = (450.0 / 460.0).toFloat(),
                    showColorOnSecondBar = true,
                    bar2Color = ProgressColor.PrimaryLite,
                    bar2LiteColor = ProgressColor.Primary,
                    userHint = UserHint.NoHint
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SetTargetOverBudgetAssignedAllBudgetedSpentPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(100.00),
                assignedMoney = Money(120.00),
                availableMoney = Money(20.00),
                progress = Progress(
                    bar1 = (100.0 / 120.0).toFloat(),
                    bar1Lite = ((120.0 - 20.0) / 120.0).toFloat(),
                    bar1Color = ProgressColor.Green,
                    bar1LiteColor = ProgressColor.GreenLite,
                    bar2 = (100.0 / 120.0).toFloat(),
                    bar2Lite = (100 / 120.0).toFloat(),
                    showColorOnSecondBar = true,
                    bar2Color = ProgressColor.Primary,
                    userHint = UserHint.ExtraMoney(amount = "20,-€")
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SetTargetOverBudgetAssignedLittleSpentPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(100.00),
                assignedMoney = Money(120.00),
                availableMoney = Money(80.00),
                progress = Progress(
                    bar1 = (100.0 / 120.0).toFloat(),
                    bar1Lite = ((120.0 - 80.0) / 120.0).toFloat(),
                    bar1Color = ProgressColor.Green,
                    bar1LiteColor = ProgressColor.GreenLite,
                    bar2 = (100.0 / 120.0).toFloat(),
                    bar2Lite = (100 / 120.0).toFloat(),
                    showColorOnSecondBar = true,
                    bar2Color = ProgressColor.Primary,
                    userHint = UserHint.ExtraMoney(amount = "20,-€")
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SetTargetFullyAssignedAllSpentPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(100.00),
                assignedMoney = Money(100.00),
                availableMoney = Money(0.00),
                progress = Progress(
                    bar1 = 1f,
                    bar1Color = ProgressColor.GreenLite
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SetTargetFullyAssignedLittleSpentPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(100.00),
                assignedMoney = Money(100.00),
                availableMoney = Money(70.00),
                progress = Progress(
                    bar1 = 1f,
                    bar1Lite = ((100.0 - 70.0) / 100.0).toFloat(),
                    bar1Color = ProgressColor.Green,
                    bar1LiteColor = ProgressColor.GreenLite
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SetTargetSomethingAssignedAllSpentPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(100.00),
                assignedMoney = Money(60.00),
                availableMoney = Money(40.00),
                progress = Progress(
                    bar1 = (60.0 / 100.0).toFloat(),
                    bar1Color = ProgressColor.YellowLite,
                    userHint = UserHint.MoreNeedForBudgetTarget(amount = "40,0€")
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SetTargetSomethingAssignedLittleSpentPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(100.00),
                assignedMoney = Money(60.00),
                availableMoney = Money(40.00),
                progress = Progress(
                    bar1 = (60.0 / 100.0).toFloat(),
                    bar1Lite = ((60.0 - 40.0) / 100.0).toFloat(),
                    bar1Color = ProgressColor.Yellow,
                    bar1LiteColor = ProgressColor.YellowLite,
                    userHint = UserHint.MoreNeedForBudgetTarget(amount = "40,0€")
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SetTargetOverBudgetAssignedNothingSpentPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(100.00),
                assignedMoney = Money(120.00),
                availableMoney = Money(120.00),
                progress = Progress(
                    bar1 = (100.00 / 120.00).toFloat(),
                    bar2 = (100.00 / 120.00).toFloat(),
                    bar2Lite = (100.00 / 120.00).toFloat(),
                    bar1Color = ProgressColor.Green,
                    bar2Color = ProgressColor.Primary,
                    showColorOnSecondBar = true,
                    userHint = UserHint.ExtraMoney(amount = "20,-€")
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SetTargetFullyAssignedNothingSpentPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(100.00),
                assignedMoney = Money(100.00),
                availableMoney = Money(100.00),
                progress = Progress(
                    bar1 = 1f,
                    bar1Color = ProgressColor.Green,
                    userHint = UserHint.FullyFunded
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SetTargetSomethingAssignedNothingSpentPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(100.00),
                assignedMoney = Money(30.00),
                availableMoney = Money(30.00),
                progress = Progress(
                    bar1 = (30.0 / 100.0).toFloat(),
                    bar2 = (30.0 / 100.0).toFloat(),
                    bar1Color = ProgressColor.Yellow,
                    userHint = UserHint.MoreNeedForBudgetTarget(amount = "70,-€")
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SetTargetNothingAssignedNothingSpentPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(100.00),
                assignedMoney = Money(0.00),
                availableMoney = Money(0.00),
                progress = Progress(
                    bar1 = 1f,
                    bar1Color = ProgressColor.Grey,
                    userHint = UserHint.MoreNeedForBudgetTarget(amount = "100,-€")
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun NoTargetSomethingAssignedOverspentPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(0.00),
                assignedMoney = Money(100.00),
                availableMoney = Money(-20.00),
                progress = Progress(
                    bar1 = 1f,
                    bar1Lite = (100.0 / 120.0).toFloat(),
                    bar1Color = ProgressColor.Red,
                    bar1LiteColor = ProgressColor.GreenLite,
                    userHint = UserHint.SpentMoreThanAvailable(amount = "20,-€")
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun NoTargetNothingAssignedOverspentPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(0.00),
                assignedMoney = Money(0.00),
                availableMoney = Money(-20.00),
                progress = Progress(
                    bar1 = 1f,
                    bar1Color = ProgressColor.Red,
                    userHint = UserHint.AssignMoreOrRemoveSpending(amount = "20,-€")
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun NoTargetSomeAssignedFullySpentPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(0.00),
                assignedMoney = Money(100.00),
                availableMoney = Money(0.00),
                progress = Progress(
                    bar1Lite = 1f,
                    bar1LiteColor = ProgressColor.GreenLite
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun NoTargetSomeAssignedLittleSpentPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(0.00),
                assignedMoney = Money(100.00),
                availableMoney = Money(80.00),
                progress = Progress(
                    bar1 = 1f,
                    bar1Lite = 0.2f,
                    bar1Color = ProgressColor.Green,
                    bar1LiteColor = ProgressColor.GreenLite
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun NoTargetSomeAssignedNoSpentPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(0.00),
                assignedMoney = Money(100.00),
                availableMoney = Money(100.00),
                progress = Progress(
                    bar1 = 1f,
                    bar1Color = ProgressColor.Green
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
fun EmptyPreview() {
    GetALifeTheme {
        Surface {
            Category(
                emoji = "🏠",
                categoryName = "Rent",
                budgetTarget = Money(940.00),
                assignedMoney = Money(0.0),
                availableMoney = Money(0.0),
                progress = EmptyProgress(),
            )
        }
    }
}