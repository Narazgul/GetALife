package app.tinygiants.getalife.presentation.onboarding.step1

import androidx.compose.runtime.Immutable
import app.tinygiants.getalife.presentation.main_app.shared_composables.ErrorMessage
import app.tinygiants.getalife.presentation.main_app.shared_composables.UiText

@Immutable
data class Step1UiState(
    val quote: UiText,
    val appName: UiText,
    val isLoading: Boolean,
    val errorMessage: ErrorMessage?
)

sealed class UserClickEvent {
    data object NavigateToNextScreen : UserClickEvent()
}