package app.tinygiants.getalife.presentation.general

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.domain.usecase.inappreview.ObserveInAppReviewRequestsUseCase
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@Composable
fun ComponentActivity.InAppReview() {
    val viewModel: InAppReviewViewModel = hiltViewModel()
    val isRequestingInAppReview by viewModel.isRequestingInAppReview.collectAsStateWithLifecycle()
    val onInAppReviewRequestCompleted = viewModel::onInAppReviewRequestCompleted

    LaunchedEffect(isRequestingInAppReview) {

        if (!isRequestingInAppReview) return@LaunchedEffect

        val reviewManager = ReviewManagerFactory.create(this@InAppReview)
        val requestFlow = withTimeoutOrNull(timeout = 5.seconds) { reviewManager.requestReviewFlow() }

        if (requestFlow == null) {
            onInAppReviewRequestCompleted()
            return@LaunchedEffect
        }

        requestFlow
            .addOnSuccessListener { reviewInfo ->
                reviewManager.launchReviewFlow(this@InAppReview, reviewInfo)
                    .addOnCompleteListener { onInAppReviewRequestCompleted() }
            }
            .addOnFailureListener { exception ->
                when (exception) {
                    is ReviewException -> Firebase.crashlytics.recordException(exception)
                    else -> Firebase.crashlytics.recordException(exception)
                }
                onInAppReviewRequestCompleted()
            }
    }
}

@HiltViewModel
class InAppReviewViewModel @Inject constructor(private val observeInAppReview: ObserveInAppReviewRequestsUseCase) : ViewModel() {
    private val _isRequestingInAppReview = MutableStateFlow(false)
    val isRequestingInAppReview = _isRequestingInAppReview.asStateFlow()

    init {
        loadInAppReviewRequests()
    }

    private fun loadInAppReviewRequests() {
        viewModelScope.launch {
            observeInAppReview().collect {
                triggerInAppReview()
            }
        }
    }

    fun onInAppReviewRequestCompleted() = _isRequestingInAppReview.update { false }
    private fun triggerInAppReview() = _isRequestingInAppReview.update { true }
}