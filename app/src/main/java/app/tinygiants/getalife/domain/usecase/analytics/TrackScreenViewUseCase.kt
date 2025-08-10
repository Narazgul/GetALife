package app.tinygiants.getalife.domain.usecase.analytics

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject

class TrackScreenViewUseCase @Inject constructor(
    private val analytics: FirebaseAnalytics
) {

    operator fun invoke(screenName: String, screenClass: String = "Screen") {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}