package app.tinygiants.getalife.presentation.account

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import app.tinygiants.getalife.presentation.account.composables.AccountDialog
import app.tinygiants.getalife.presentation.account.composables.AccountsList
import app.tinygiants.getalife.presentation.budget.composables.ANIMATION_TIME_1_SECOND
import app.tinygiants.getalife.presentation.budget.composables.ANIMATION_TIME_300_MILLISECONDS
import app.tinygiants.getalife.presentation.composables.ErrorMessage
import app.tinygiants.getalife.presentation.composables.LoadingIndicator
import app.tinygiants.getalife.theme.GetALifeTheme

@Composable
fun AccountScreen() {
    val viewModel: AccountViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AccountScreen(
        uiState = uiState,
        onUserClickEvent = viewModel::onUserClickEvent
    )
}

@Composable
fun AccountScreen(
    uiState: AccountUiState,
    onUserClickEvent: (UserClickEvent) -> Unit = { }
) {

    var showAddAccountDialog by remember { mutableStateOf(false) }

    val onAddAccountClicked = { accountName: String, balance: Money, type: AccountType ->
        onUserClickEvent(UserClickEvent.AddAccount(name = accountName, balance = balance, type = type))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AccountsList(
            accounts = uiState.accounts,
            categories = uiState.categories,
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
        TextButton(
            onClick = { showAddAccountDialog = true },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Text(text = "Account hinzufügen")
        }
        AnimatedVisibility(
            visible = showAddAccountDialog,
            enter = fadeIn(animationSpec = tween(ANIMATION_TIME_1_SECOND)),
            exit = fadeOut(animationSpec = tween(ANIMATION_TIME_300_MILLISECONDS))
        ) {
            AccountDialog(
                onConfirmClicked = onAddAccountClicked,
                onDismissRequest = { showAddAccountDialog = false }
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
                    errorMessage = null
                )
            )
        }
    }
}

fun accounts() = listOf(
    Account(id = 1L, name = "Cash", balance = Money(value = 0.00), type = AccountType.Cash, listPosition = 0),
    Account(
        id = 2L,
        name = "Girokonto",
        balance = Money(value = -100.00),
        type = AccountType.Checking,
        listPosition = 1
    ),
    Account(
        id = 3L,
        name = "Tagesgeldkonto",
        balance = Money(value = 100.00),
        type = AccountType.Savings,
        listPosition = 2
    )
)