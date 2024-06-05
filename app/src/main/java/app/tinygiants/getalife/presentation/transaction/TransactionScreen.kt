package app.tinygiants.getalife.presentation.transaction

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.presentation.budget.UserClickEvent
import app.tinygiants.getalife.presentation.transaction.composables.TransactionsList
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.ScreenPreview

@Composable
fun TransactionScreen() {
    val viewModel: TransactionViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TransactionScreen(uiState = uiState)
}

@Composable
fun TransactionScreen(
    uiState: TransactionUiState,
    onUserClickEvent: (UserClickEvent) -> Unit = { }
) {

    TransactionsList(title = uiState.title)
}

@ScreenPreview
@Composable
private fun TransactionScreenPreview() {
    GetALifeTheme {
        Surface {
            TransactionScreen()
        }
    }
}