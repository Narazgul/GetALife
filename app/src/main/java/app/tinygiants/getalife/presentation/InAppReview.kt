package app.tinygiants.getalife.presentation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.seconds

@Composable
fun RequestInAppReview(
    activity: Activity,
    isRequestingInAppReview: Boolean,
    onInAppReviewRequestCompleted: () -> Unit
) {
    LaunchedEffect(isRequestingInAppReview) {

        if (!isRequestingInAppReview) return@LaunchedEffect

        val reviewManager = ReviewManagerFactory.create(activity)
        val requestFlow = withTimeoutOrNull(timeout = 5.seconds) { reviewManager.requestReviewFlow() }

        if (requestFlow == null) {
            onInAppReviewRequestCompleted()
            return@LaunchedEffect
        }

        requestFlow
            .addOnSuccessListener { reviewInfo ->
                reviewManager.launchReviewFlow(activity, reviewInfo)
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