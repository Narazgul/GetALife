package app.tinygiants.getalife.presentation.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.R
import app.tinygiants.getalife.domain.usecase.onboarding.GetOnboardingUseCase
import app.tinygiants.getalife.presentation.UiText.DynamicString
import app.tinygiants.getalife.presentation.UiText.StringResource
import app.tinygiants.getalife.presentation.shared_composables.ErrorMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(private val getOnboarding: GetOnboardingUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OnboardingUiState(
            title = "",
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
//                getOnboarding()
//                    .catch { throwable -> displayErrorState(throwable = throwable) }
//                    .collect { result ->
//                        result.onFailure { throwable -> displayErrorState(throwable = throwable) }
//                        result.onSuccess { title -> displayTitle(title = title) }
//                    }
            }
        }
    }

    // endregion

    // region Interaction from UI

    fun onUserClickEvent(clickEvent: UserClickEvent) {
        viewModelScope.launch {
            when (clickEvent) {
                UserClickEvent.Click -> Log.d("OnboardingViewModel", "Clicked")
            }
        }
    }

    // region Private

    private fun displayTitle(title: String) {
        _uiState.update { onboardingUiState ->
            onboardingUiState.copy(title = title)
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
                title = "",
                isLoading = false,
                errorMessage = errorMessage
            )
        }
    }

    //endregion
}