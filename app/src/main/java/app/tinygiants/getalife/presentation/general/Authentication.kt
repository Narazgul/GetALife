package app.tinygiants.getalife.presentation.general

import androidx.compose.runtime.Composable
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.crashlytics.crashlytics
import com.revenuecat.purchases.Purchases
import com.superwall.sdk.Superwall
import com.superwall.sdk.identity.identify

@Composable
fun Authentication() {
    Firebase.auth.addAuthStateListener { auth ->

        val currentUserUid = auth.currentUser?.uid

        if (currentUserUid == null)
            auth.signInAnonymously()
                .addOnFailureListener { exception ->
                    Firebase.crashlytics.recordException(exception)
                }
        else {
            Firebase.crashlytics.setUserId(currentUserUid)

            // Safely identify with Superwall
            identifyWithSuperwall(currentUserUid)

            Purchases.sharedInstance.logIn(currentUserUid) // RevenueCat
        }
    }
}

private fun identifyWithSuperwall(userId: String) {
    try {
        // Attempt to identify with Superwall
        Superwall.instance.identify(userId)
    } catch (e: UninitializedPropertyAccessException) {
        // Superwall not yet initialized - this is not critical for app functionality
        Firebase.crashlytics.recordException(e)
    } catch (e: Exception) {
        // Other potential errors with Superwall
        Firebase.crashlytics.recordException(e)
    }
}