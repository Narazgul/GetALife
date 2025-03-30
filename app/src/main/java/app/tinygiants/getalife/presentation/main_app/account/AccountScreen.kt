package app.tinygiants.getalife.presentation.main_app.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.presentation.main_app.account.composables.AccountsList
import app.tinygiants.getalife.presentation.main_app.account.composables.AddAccountBottomSheet
import app.tinygiants.getalife.presentation.shared_composables.ErrorMessage
import app.tinygiants.getalife.presentation.shared_composables.LoadingIndicator
import app.tinygiants.getalife.theme.GetALifeTheme
import kotlinx.datetime.Clock

@Composable
fun AccountScreen(onNavigateToTransactionScreen: (accountId: Long) -> Unit) {
    val viewModel: AccountViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AccountScreen(
        uiState = uiState,
        onNavigateToTransactionScreen = onNavigateToTransactionScreen,
        onUserClickEvent = viewModel::onUserClickEvent,
        onUserMessageShown = viewModel::onUserMessageShown
    )
}

@Composable
fun AccountScreen(
    uiState: AccountUiState,
    onNavigateToTransactionScreen: (accountId: Long) -> Unit = { },
    onUserClickEvent: (UserClickEvent) -> Unit = { },
    onUserMessageShown: () -> Unit = { }
) {

    var areFabButtonsVisible by rememberSaveable { mutableStateOf(true) }
    var isAddAccountBottomSheetVisible by remember { mutableStateOf(false) }

    val startingBalanceString = stringResource(R.string.starting_balance)
    val startingBalanceDescription = stringResource(R.string.starting_balance_description)

    val snackbarHostState = remember { SnackbarHostState() }
    uiState.userMessage?.let { userMessage ->
        val userMessageString = userMessage.asString()
        LaunchedEffect(userMessage) {
            snackbarHostState.showSnackbar(userMessageString)
            onUserMessageShown()
        }
    }

    val onAddAccountClicked = { accountName: String, startingBalance: Money, type: AccountType ->
        onUserClickEvent(
            UserClickEvent.AddAccount(
                name = accountName,
                balance = startingBalance,
                type = type,
                startingBalanceName = startingBalanceString,
                startingBalanceDescription = startingBalanceDescription
            )
        )
    }

    Column {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        Scaffold(
            floatingActionButton = {
                AnimatedVisibility(
                    visible = areFabButtonsVisible,
                    enter = fadeIn(tween(500)),
                    exit = fadeOut(tween(500))
                ) {
                    ExtendedFloatingActionButton(
                        onClick = { isAddAccountBottomSheetVisible = true },
                        icon = { Icon(Icons.Filled.Add, "Add Account FloatingActionButton") },
                        text = { Text(text = stringResource(id = R.string.add_account)) }
                    )
                }
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
            ) { innerPadding ->
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

            Box(
                modifier = Modifier
                    .consumeWindowInsets(innerPadding)
                    .fillMaxSize()
            ) {
                if (uiState.accounts.isEmpty() && !uiState.isLoading)
                    Image(
                        painter = painterResource(id = R.drawable.bg_cactus),
                        contentDescription = "No groups available",
                        modifier = Modifier.fillMaxSize(),
                        alignment = Alignment.BottomCenter
                    )

                AccountsList(
                    accounts = uiState.accounts,
                    onNavigateToTransactionScreen = onNavigateToTransactionScreen,
                    onUserScrolling = { isUserScrollingDown -> areFabButtonsVisible = isUserScrollingDown },
                    onUserClickEvent = onUserClickEvent
                )
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

            if (isAddAccountBottomSheetVisible) AddAccountBottomSheet(
                onConfirmClicked = onAddAccountClicked,
                onDismissRequest = { isAddAccountBottomSheetVisible = false }
            )
        }
    }
}

@PreviewLightDark
@Composable
fun AccountScreenPreview() {
    GetALifeTheme {
        Surface {
            AccountScreen(
                uiState = AccountUiState(
                    accounts = accounts(),
                    categories = emptyList(),
                    isLoading = false,
                    userMessage = null,
                    errorMessage = null
                )
            )
        }
    }
}

fun accounts() = listOf(
    Account(
        id = 1L,
        name = "Cash",
        balance = Money(value = 0.00),
        type = AccountType.Cash,
        listPosition = 0,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    ),
    Account(
        id = 2L,
        name = "Girokonto",
        balance = Money(value = -100.00),
        type = AccountType.Checking,
        listPosition = 1,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    ),
    Account(
        id = 3L,
        name = "Tagesgeldkonto",
        balance = Money(value = 100.00),
        type = AccountType.Savings,
        listPosition = 2,
        updatedAt = Clock.System.now(),
        createdAt = Clock.System.now()
    )
)