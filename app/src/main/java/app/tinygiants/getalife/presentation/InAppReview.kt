package app.tinygiants.getalife.presentation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

@Composable
fun RequestInAppReview(
    activity: Activity,
    isRequestingInAppReview: Boolean,
    onInAppReviewRequestCompleted: () -> Unit
) {
    LaunchedEffect(isRequestingInAppReview) {

        if (!isRequestingInAppReview) return@LaunchedEffect

        val reviewManager = ReviewManagerFactory.create(activity)
        val requestFlow = reviewManager.requestReviewFlow()

        requestFlow
            .addOnSuccessListener { reviewInfo ->
                reviewManager.launchReviewFlow(activity, reviewInfo)
                    .addOnCompleteListener { onInAppReviewRequestCompleted() }
            }
            .addOnFailureListener { exception ->
                Firebase.crashlytics.recordException(exception as ReviewException)
                onInAppReviewRequestCompleted()
            }
    }
}