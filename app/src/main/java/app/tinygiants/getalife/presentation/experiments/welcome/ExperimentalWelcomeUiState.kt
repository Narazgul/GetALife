package app.tinygiants.getalife.presentation.experiments.welcome

import androidx.compose.runtime.Immutable
import app.tinygiants.getalife.presentation.shared_composables.ErrorMessage
import app.tinygiants.getalife.presentation.shared_composables.UiText

@Immutable
data class ExperimentalWelcomeUiState(
    val quote: UiText,
    val appName: UiText,
    val isLoading: Boolean,
    val errorMessage: ErrorMessage?
)

sealed class UserClickEvent {
    data object NavigateToNextScreen : UserClickEvent()
}