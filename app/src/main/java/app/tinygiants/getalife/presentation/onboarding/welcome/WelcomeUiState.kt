package app.tinygiants.getalife.presentation.onboarding.welcome

import androidx.compose.runtime.Immutable
import app.tinygiants.getalife.presentation.main_app.shared_composables.ErrorMessage
import app.tinygiants.getalife.presentation.main_app.shared_composables.UiText

@Immutable
data class WelcomeUiState(
    val quote: UiText,
    val appName: UiText,
    val isLoading: Boolean,
    val errorMessage: ErrorMessage?
)

sealed class UserClickEvent {
    data object NavigateToNextScreen : UserClickEvent()
}