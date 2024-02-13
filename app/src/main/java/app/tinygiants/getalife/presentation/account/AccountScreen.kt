package app.tinygiants.getalife.presentation.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.theme.GetALifeTheme
import app.tinygiants.getalife.theme.LightAndDarkPreviews
import app.tinygiants.getalife.theme.spacing

@Composable
fun AccountScreen() {
    val viewModel: AccountViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AccountScreen(uiState = uiState)
}

@Composable
fun AccountScreen(
    uiState: AccountUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(all = spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = uiState.title, style = MaterialTheme.typography.displayMedium)
        Text(text = uiState.subtitle, style = MaterialTheme.typography.bodyMedium)
    }
}

@LightAndDarkPreviews
@Composable
fun AccountScreenPreview() {
    GetALifeTheme {
        Surface {
            AccountScreen(
                AccountUiState(
                    title = "Account Screen",
                    subtitle = "not yet implemented",
                    isLoading = false,
                )
            )
        }
    }
}