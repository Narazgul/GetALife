package app.tinygiants.getalife.presentation.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.tinygiants.getalife.theme.GetALifeTheme

@Composable
fun OnboardingScreen() {
    val viewModel: OnboardingViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OnboardingScreen(
        uiState = uiState,
        onUserClickEvent = viewModel::onUserClickEvent
    )
}

@Composable
fun OnboardingScreen(
    uiState: OnboardingUiState,
    onUserClickEvent: (UserClickEvent) -> Unit = { }
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = uiState.title,
            modifier = Modifier.clickable {
                onUserClickEvent(UserClickEvent.Click)
            })
    }
}

@Preview
@Composable
private fun OnboardingScreenPreview(@PreviewParameter(OnboardingScreenPreviewProvider::class) uiState: OnboardingUiState) {
    GetALifeTheme {
        Surface {
            OnboardingScreen(uiState = uiState)
        }
    }
}

class OnboardingScreenPreviewProvider : PreviewParameterProvider<OnboardingUiState> {
    override val values: Sequence<OnboardingUiState>
        get() = sequenceOf(
            OnboardingUiState(
                title = "OnboardingScreen",
                isLoading = false,
                errorMessage = null
            )
        )
}