package app.tinygiants.getalife.presentation.budget

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.BudgetPurpose
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Header
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.presentation.budget.composables.AddGroupBottomSheet
import app.tinygiants.getalife.presentation.budget.composables.AssignableMoney
import app.tinygiants.getalife.presentation.budget.composables.BudgetList
import app.tinygiants.getalife.presentation.composables.ErrorMessage
import app.tinygiants.getalife.presentation.composables.LoadingIndicator
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.ScreenPreview
import app.tinygiants.getalife.theme.spacing

@Composable
fun BudgetScreen() {
    val viewModel: BudgetViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BudgetScreen(
        uiState = uiState,
        onUserClickEvent = viewModel::onUserClickEvent
    )
}

@Composable
fun BudgetScreen(
    uiState: BudgetUiState,
    onUserClickEvent: (UserClickEvent) -> Unit = { }
) {
    val focusManager = LocalFocusManager.current

    var isAddGroupFabVisible by rememberSaveable { mutableStateOf(true) }
    var isAddGroupBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

    Column {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        Scaffold(
            floatingActionButton = {
                AnimatedVisibility(
                    visible = isAddGroupFabVisible,
                    enter = fadeIn(tween(500)),
                    exit = fadeOut(tween(500))
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { isAddGroupBottomSheetVisible = true },
                        icon = { Icon(Icons.Filled.Add, "Add Group FloatingActionButton") },
                        text = { Text(text = stringResource(id = R.string.add_group)) }
                    )
                }
            }) { innerPadding ->
            Box(
                modifier = Modifier
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
            ) {
                Column(modifier = Modifier.align(Alignment.TopCenter)) {

                    if (uiState.groups.isEmpty() && !uiState.isLoading)
                        Image(
                            painter = painterResource(id = R.drawable.bg_city),
                            contentDescription = "No groups available",
                            modifier = Modifier.fillMaxSize(),
                            alignment = Alignment.BottomCenter
                        )

                    AnimatedVisibility(
                        visible = uiState.assignableMoney != null && uiState.assignableMoney.value != 0.00,
                        enter = fadeIn(tween(1500)),
                        exit = fadeOut(tween(3000)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.default)
                    ) { if (uiState.assignableMoney != null) AssignableMoney(assignableMoney = uiState.assignableMoney) }

                    BudgetList(
                        groups = uiState.groups,
                        isLoading = uiState.isLoading,
                        errorMessage = uiState.errorMessage,
                        onUserScrolling = { isUserScrollingDown -> isAddGroupFabVisible = isUserScrollingDown },
                        onUserClickEvent = onUserClickEvent
                    )
                }
                LoadingIndicator(
                    isLoading = uiState.isLoading,
                    errorMessage = uiState.errorMessage,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
                ErrorMessage(
                    errorMessage = uiState.errorMessage,
                    modifier = Modifier.align(Alignment.TopCenter)
                )

            }
        }
    }

    if (isAddGroupBottomSheetVisible) {
        AddGroupBottomSheet(
            onDismissRequest = { isAddGroupBottomSheetVisible = false },
            onUserClickEvent = onUserClickEvent
        )
    }
}

@ScreenPreview
@Composable
fun BudgetScreenPreview(@PreviewParameter(BudgetScreenPreviewProvider::class) uiState: BudgetUiState) {
    GetALifeTheme {
        Surface {
            BudgetScreen(uiState = uiState)
        }
    }
}

class BudgetScreenPreviewProvider : PreviewParameterProvider<BudgetUiState> {
    override val values: Sequence<BudgetUiState>
        get() = sequenceOf(
            BudgetUiState(
                assignableMoney = Money(value = 100.00),
                groups = fakeCategories(),
                isLoading = false,
                errorMessage = null
            )
        )
}

fun fakeCategories() = mapOf(
    Header(
        id = 0,
        name = "Fixed Costs",
        sumOfAvailableMoney = Money(value = 1015.00),
        listPosition = 0,
        isExpanded = true
    ) to listOf(
        Category(
            id = 1,
            headerId = 0,
            emoji = "🏠",
            name = "Rent",
            budgetTarget = Money(value = 1000.00),
            budgetPurpose = BudgetPurpose.Spending,
            assignedMoney = Money(value = 1000.00),
            availableMoney = Money(value = 1000.00),
            progress = (1000.00 / 1000.00).toFloat(),
            spentProgress = 0.2f,
            overspentProgress = 0f,
            budgetTargetProgress = 0.8f,
            optionalText = "",
            listPosition = 0,
            isInitialCategory = false
        ),
        Category(
            id = 2,
            headerId = 0,
            emoji = "🏥",
            name = "Barmenia",
            budgetTarget = Money(value = 28.55),
            budgetPurpose = BudgetPurpose.Spending,
            assignedMoney = Money(value = 28.55),
            availableMoney = Money(value = 28.55),
            progress = (28.55 / 28.55).toFloat(),
            spentProgress = 0.2f,
            overspentProgress = 0f,
            budgetTargetProgress = 0f,
            optionalText = "",
            listPosition = 1,
            isInitialCategory = false
        ),
        Category(
            id = 3,
            headerId = 0,
            emoji = "📱",
            name = "Fraenk",
            budgetTarget = Money(value = 10.00),
            budgetPurpose = BudgetPurpose.Spending,
            assignedMoney = Money(value = 10.00),
            availableMoney = Money(value = 10.00),
            progress = (10.00 / 10.00).toFloat(),
            spentProgress = 0f,
            overspentProgress = 0f,
            budgetTargetProgress = 0f,
            optionalText = "",
            listPosition = 2,
            isInitialCategory = false
        ),
        Category(
            id = 4,
            headerId = 0,
            emoji = "▶️",
            name = "YouTube Premium",
            budgetTarget = Money(value = 7.49),
            budgetPurpose = BudgetPurpose.Spending,
            assignedMoney = Money(value = 7.49),
            availableMoney = Money(value = 7.49),
            progress = (7.49 / 7.49).toFloat(),
            spentProgress = 1f,
            overspentProgress = 0f,
            budgetTargetProgress = 0f,
            optionalText = "",
            listPosition = 3,
            isInitialCategory = false
        ),
        Category(
            id = 5,
            headerId = 0,
            emoji = "🏋",
            name = "Gym",
            budgetTarget = Money(value = 29.00),
            budgetPurpose = BudgetPurpose.Spending,
            assignedMoney = Money(value = 13.00),
            availableMoney = Money(value = 13.00),
            progress = (13 / 29.00).toFloat(),
            spentProgress = 0.1f,
            overspentProgress = 0f,
            budgetTargetProgress = 0f,
            optionalText = "16.00 € more needed by the 30th",
            listPosition = 4,
            isInitialCategory = false
        ),
        Category(
            id = 6,
            headerId = 0,
            emoji = "🍿",
            name = "Netflix",
            budgetTarget = Money(value = 9.00),
            budgetPurpose = BudgetPurpose.Spending,
            assignedMoney = Money(value = 2.00),
            availableMoney = Money(value = 2.00),
            progress = (2 / 9.00).toFloat(),
            spentProgress = 0f,
            overspentProgress = 0f,
            budgetTargetProgress = 0f,
            optionalText = "7.00 € mored needed by the 10th",
            listPosition = 5,
            isInitialCategory = false
        )
    ),
    Header(
        id = 100,
        name = "Daily Life",
        sumOfAvailableMoney = Money(value = 210.0),
        listPosition = 1,
        isExpanded = true
    ) to listOf(
        Category(
            id = 101,
            headerId = 4,
            emoji = "🛒",
            name = "Groceries",
            budgetTarget = Money(value = 100.00),
            budgetPurpose = BudgetPurpose.Spending,
            assignedMoney = Money(value = 27.00),
            availableMoney = Money(value = 27.00),
            progress = (27.00 / 100.00).toFloat(),
            spentProgress = 0f,
            overspentProgress = 0f,
            budgetTargetProgress = 0f,
            optionalText = "73.00 € more needed by the 30th",
            listPosition = 0,
            isInitialCategory = false
        ),
        Category(
            id = 102,
            headerId = 4,
            emoji = "🚌",
            name = "Transportation",
            budgetTarget = Money(value = 50.00),
            budgetPurpose = BudgetPurpose.Spending,
            assignedMoney = Money(value = 0.00),
            availableMoney = Money(value = 0.00),
            progress = (0 / 50.00).toFloat(),
            spentProgress = 0f,
            overspentProgress = 0f,
            budgetTargetProgress = 0f,
            optionalText = "50.00 € more needed by the 30th",
            listPosition = 1,
            isInitialCategory = false
        ),
        Category(
            id = 103,
            headerId = 4,
            emoji = "🍽",
            name = "Eating Out",
            budgetTarget = Money(value = 60.00),
            budgetPurpose = BudgetPurpose.Spending,
            assignedMoney = Money(value = 60.00),
            availableMoney = Money(value = 60.00),
            progress = (60 / 60.00).toFloat(),
            spentProgress = 0.9f,
            overspentProgress = 0f,
            budgetTargetProgress = 0f,
            optionalText = "",
            listPosition = 2,
            isInitialCategory = false
        )
    ),
    Header(
        id = 200,
        name = "Daily Life",
        sumOfAvailableMoney = Money(value = 0.0),
        listPosition = 2,
        isExpanded = true
    ) to listOf(
        Category(
            id = 201,
            emoji = "",
            name = "Jetzt Kategorie hinzufügen",
            headerId = 8,
            budgetTarget = Money(value = 60.00),
            budgetPurpose = BudgetPurpose.Unknown,
            assignedMoney = Money(value = 60.00),
            availableMoney = Money(value = 60.00),
            progress = 0f,
            spentProgress = 0f,
            overspentProgress = 0f,
            budgetTargetProgress = 0f,
            optionalText = "",
            listPosition = 0,
            isInitialCategory = true
        )
    ),
    Header(
        id = 300,
        name = "Goals",
        sumOfAvailableMoney = Money(value = 0.0),
        listPosition = 3,
        isExpanded = false
    ) to emptyList()
)