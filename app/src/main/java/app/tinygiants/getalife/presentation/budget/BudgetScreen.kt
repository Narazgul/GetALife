package app.tinygiants.getalife.presentation.budget

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.presentation.budget.composables.LoadingIndicator
import app.tinygiants.getalife.presentation.budget.composables.BudgetsList
import app.tinygiants.getalife.presentation.budget.composables.ErrorMessage
import app.tinygiants.getalife.theme.GetALifeTheme

@Composable
fun BudgetScreen() {
    val viewModel: BudgetViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    BudgetScreen(uiState)
}

@Composable
fun BudgetScreen(uiState: BudgetUiState) {
    Box(modifier = Modifier.fillMaxSize().padding(8.dp)
    ) {
        BudgetsList(
            categories = uiState.categories,
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage
        )
        LoadingIndicator(
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage
        )
        ErrorMessage(uiState.errorMessage)
    }
}

@Preview(name = "Light", widthDp = 400)
@Preview(name = "Dark", widthDp = 400, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun BudgetScreenPreview() {
    GetALifeTheme {
        Surface {
            BudgetScreen(
                BudgetUiState(
                    categories = emptyMap(),
                    isLoading = true,
                    errorMessage = "zefix"
                )
            )
        }
    }
}