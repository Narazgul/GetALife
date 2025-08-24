package app.tinygiants.getalife.presentation.main_app.budget

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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.CategoryMonthlyStatus
import app.tinygiants.getalife.domain.model.EmptyProgress
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.presentation.main_app.budget.BannerUiState.AssignableMoneyAvailable
import app.tinygiants.getalife.presentation.main_app.budget.composables.AssignableMoney
import app.tinygiants.getalife.presentation.main_app.budget.composables.BudgetList
import app.tinygiants.getalife.presentation.main_app.budget.composables.MonthNavigator
import app.tinygiants.getalife.presentation.main_app.budget.composables.group.AddGroupBottomSheet
import app.tinygiants.getalife.presentation.shared_composables.ErrorMessage
import app.tinygiants.getalife.presentation.shared_composables.LoadingIndicator
import app.tinygiants.getalife.presentation.shared_composables.UiText.DynamicString
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.ScreenPreview
import app.tinygiants.getalife.theme.spacing
import kotlinx.datetime.YearMonth
import kotlin.time.Clock
import kotlinx.datetime.Month

@Composable
fun BudgetScreen() {
    val viewModel: BudgetViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentMonth by viewModel.currentMonth.collectAsStateWithLifecycle()

    BudgetScreen(
        uiState = uiState,
        currentMonth = currentMonth,
        onUserClickEvent = viewModel::onUserClickEvent,
        onUserMessageShown = viewModel::onUserMessageShown,
        onMonthChanged = viewModel::navigateToMonth
    )
}

@Composable
fun BudgetScreen(
    uiState: BudgetUiState,
    currentMonth: YearMonth,
    onUserClickEvent: (UserClickEvent) -> Unit = { },
    onUserMessageShown: () -> Unit = { },
    onMonthChanged: (YearMonth) -> Unit = { }
) {
    val focusManager = LocalFocusManager.current

    var isAddGroupFabVisible by rememberSaveable { mutableStateOf(true) }
    var isAddGroupBottomSheetVisible by rememberSaveable { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    uiState.userMessage?.let { userMessage ->
        val userMessageString = userMessage.asString()
        LaunchedEffect(userMessage) {
            snackbarHostState.showSnackbar(userMessageString)
            onUserMessageShown()
        }
    }

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
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
            ) {
                Column(modifier = Modifier.align(Alignment.TopCenter)) {

                    MonthNavigator(
                        currentMonth = currentMonth,
                        onMonthChanged = onMonthChanged
                    )

                    if (uiState.groups.isEmpty() && !uiState.isLoading)
                        Image(
                            painter = painterResource(id = R.drawable.bg_city),
                            contentDescription = "No groups available",
                            modifier = Modifier.fillMaxSize(),
                            alignment = Alignment.BottomCenter
                        )

                    AnimatedVisibility(
                        visible = uiState.bannerState !is BannerUiState.AllAssigned,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.m)
                    ) { AssignableMoney(banner = uiState.bannerState) }

                    BudgetList(
                        groups = uiState.groups,
                        creditCardAccountBalances = uiState.creditCardAccountBalances,
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
            BudgetScreen(uiState = uiState, currentMonth = YearMonth(2024, Month.JANUARY))
        }
    }
}

class BudgetScreenPreviewProvider : PreviewParameterProvider<BudgetUiState> {
    override val values: Sequence<BudgetUiState>
        get() = sequenceOf(
            BudgetUiState(
                bannerState = AssignableMoneyAvailable(text = DynamicString("Distribute available money to categories: 100,-€")),
                groups = fakeCategoryBudgets(),
                creditCardAccountBalances = emptyMap(),
                isLoading = false,
                userMessage = null,
                errorMessage = null
            )
        )
}

fun fakeCategoryBudgets() = mapOf(
    Group(
        id = 0,
        name = "Fixed Costs",
        sumOfAvailableMoney = Money(value = 1015.00),
        listPosition = 0,
        isExpanded = true
    ) to listOf(
        CategoryMonthlyStatus(
            category = Category(
                id = 1,
                groupId = 0,
                emoji = "🏠",
                name = "Rent",
                budgetTarget = Money(value = 1000.00),
                monthlyTargetAmount = null,
                targetMonthsRemaining = null,
                listPosition = 0,
                isInitialCategory = false,
                updatedAt = Clock.System.now(),
                createdAt = Clock.System.now()
            ),
            assignedAmount = Money(1000.00),
            isCarryOverEnabled = true,
            spentAmount = Money(100.0),
            availableAmount = Money(900.0),
            progress = EmptyProgress(),
            suggestedAmount = null,
            targetContribution = null
        )
    )
)