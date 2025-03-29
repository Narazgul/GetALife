package app.tinygiants.getalife.presentation.experiments.welcome

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.usecase.onboarding.GetOnboardingUseCase
import app.tinygiants.getalife.domain.usecase.onboarding.OnboardingStep
import app.tinygiants.getalife.presentation.main_app.shared_composables.ErrorMessage
import app.tinygiants.getalife.presentation.main_app.shared_composables.UiText.DynamicString
import app.tinygiants.getalife.presentation.main_app.shared_composables.UiText.StringResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExperimentalWelcomeViewModel @Inject constructor(private val getOnboarding: GetOnboardingUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ExperimentalWelcomeUiState(
            quote = DynamicString(value = ""),
            appName = DynamicString(value = ""),
            isLoading = true,
            errorMessage = null
        )
    )
    val uiState = _uiState.asStateFlow()

    // region Init

    init {
        loadOnboarding()
    }

    private fun loadOnboarding() {
        viewModelScope.launch {

            launch {
                getOnboarding()
                    .catch { throwable -> displayErrorState(throwable = throwable) }
                    .collect { result ->
                        result.onFailure { throwable -> displayErrorState(throwable = throwable) }
                        result.onSuccess { onboardingStep -> displayTitle(onboardingStep) }
                    }
            }
        }
    }

    // endregion

    // region Interaction from UI

    fun onUserClickEvent(clickEvent: UserClickEvent) {
        viewModelScope.launch {
            when (clickEvent) {
                UserClickEvent.NavigateToNextScreen -> println("Next Screen")
            }
        }
    }

    // region Private

    private fun displayTitle(onboardingStep: OnboardingStep) {
        when (onboardingStep) {
            OnboardingStep.Quote -> _uiState.update { onboardingUiState ->
                onboardingUiState.copy(
                    quote = StringResource(resId = R.string.quote),
                    appName = StringResource(resId = R.string.appName),
                    isLoading = false,
                    errorMessage = null
                )
            }
        }
    }

    private fun displayErrorState(throwable: Throwable) {
        val errorMessage = ErrorMessage(
            title = StringResource(resId = R.string.error_title),
            subtitle = if (throwable.message != null) DynamicString(value = throwable.message ?: "")
            else StringResource(resId = R.string.error_subtitle)
        )

        _uiState.update { onboardingUiState ->
            onboardingUiState.copy(
                quote = DynamicString(value = ""),
                appName = DynamicString(value = ""),
                isLoading = false,
                errorMessage = errorMessage
            )
        }
    }

    //endregion
}