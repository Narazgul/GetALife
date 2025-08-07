package app.tinygiants.getalife.presentation.analytics

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * A utility Composable that logs a screen_view event to Firebase Analytics.
 * This should be placed at the top of a screen's Composable content.
 *
 * @param screenName The name of the screen to track.
 */
@Composable
fun TrackScreenView(screenName: String) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val analytics = FirebaseAnalytics.getInstance(context)
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, "Onboarding") // Generic class for all onboarding screens
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}