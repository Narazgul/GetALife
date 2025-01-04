package app.tinygiants.getalife.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.domain.model.SubscriptionStatus
import app.tinygiants.getalife.domain.usecase.appupdate.GetAppUpdateTypeUseCase
import app.tinygiants.getalife.domain.usecase.inappreview.ObserveInAppReviewRequestsUseCase
import app.tinygiants.getalife.domain.usecase.inappreview.RequestInAppReviewUseCase
import app.tinygiants.getalife.domain.usecase.subscription.GetUserSubscriptionStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MainState(
    val subscriptionStatus: SubscriptionStatus,
    val appUpdateType: Int?,
    val isRequestingInAppReview: Boolean
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getSubscription: GetUserSubscriptionStatusUseCase,
    private val getUpdateType: GetAppUpdateTypeUseCase,
    private val observeInAppReview: ObserveInAppReviewRequestsUseCase,
    private val requestInAppReview: RequestInAppReviewUseCase
) : ViewModel() {

    private val _mainState = MutableStateFlow(
        MainState(
            subscriptionStatus = SubscriptionStatus.Unknown,
            appUpdateType = null,
            isRequestingInAppReview = false
        )
    )
    val mainState = _mainState.asStateFlow()

    // region Init

    init {
        loadSubscription()
        loadAppUpdateType()
        loadInAppReviewRequests()
    }

    private fun loadSubscription() {
        viewModelScope.launch {
            getSubscription()
                .collect { subscriptionStatus ->
                    updateSubscriptionState(subscriptionStatus)
                }
        }
    }

    private fun loadAppUpdateType() {
        viewModelScope.launch {
            getUpdateType()
                .catch { updateAppUpdateState(appUpdateState = null) }
                .collect { result ->
                    result.onSuccess { appUpdateType -> updateAppUpdateState(appUpdateState = appUpdateType) }
                    result.onFailure { updateAppUpdateState(appUpdateState = null) }
                }
        }
    }

    private fun loadInAppReviewRequests() {
        viewModelScope.launch {
            observeInAppReview().collect {
                triggerInAppReview()
            }
        }
    }

    // endregion

    // region Interaction from UI

    suspend fun triggerInAppReviewTest() = requestInAppReview()

    fun onInAppReviewRequestCompleted() = _mainState.update { mainState -> mainState.copy(isRequestingInAppReview = false) }

    // endregion

    // region Private

    private fun updateSubscriptionState(subscriptionStatus: SubscriptionStatus) =
        _mainState.update { mainState -> mainState.copy(subscriptionStatus = subscriptionStatus) }

    private fun updateAppUpdateState(appUpdateState: Int?) {
        if (appUpdateState == null) _mainState.update { mainState -> mainState.copy(appUpdateType = null) }
        else _mainState.update { mainState -> mainState.copy(appUpdateType = appUpdateState) }
    }

    private fun triggerInAppReview() = _mainState.update { mainState -> mainState.copy(isRequestingInAppReview = true) }

    // endregion
}