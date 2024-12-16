package app.tinygiants.getalife.presentation.onboarding

import androidx.compose.runtime.Immutable
import app.tinygiants.getalife.presentation.UiText
import app.tinygiants.getalife.presentation.shared_composables.ErrorMessage

@Immutable
data class OnboardingUiState(
    val quote: UiText,
    val appName: UiText,
    val isLoading: Boolean,
    val errorMessage: ErrorMessage?
)

sealed class UserClickEvent {
    data object Click : UserClickEvent()
}