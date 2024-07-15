package app.tinygiants.getalife.presentation.transaction

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.presentation.transaction.composables.TransactionsList
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.ScreenPreview

@Composable
fun TransactionScreen(onNavigateUp: () -> Unit) {
    val viewModel: TransactionViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TransactionScreen(
        uiState = uiState,
        onUserClickEvent = viewModel::onUserClickEvent,
        onNavigateUp = onNavigateUp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(
    uiState: TransactionUiState,
    onUserClickEvent: (UserClickEvent) -> Unit,
    onNavigateUp: () -> Unit
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        TransactionsList(
            modifier = Modifier.padding(innerPadding),
            transactions = uiState.transactions,
            accounts = uiState.accounts,
            categories = uiState.categories,
            onUserClickEvent = onUserClickEvent
        )
    }
}

@ScreenPreview
@Composable
private fun TransactionScreenPreview() {
    GetALifeTheme {
        Surface {
            TransactionScreen(
                uiState = TransactionUiState(
                    title = "Girokonto",
                    transactions = emptyList(),
                    accounts = emptyList(),
                    categories = emptyList(),
                    isLoading = false,
                    errorMessage = null
                ),
                onUserClickEvent = {},
                onNavigateUp = {}
            )
        }
    }
}