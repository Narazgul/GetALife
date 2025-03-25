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
            Superwall.instance.identify(currentUserUid)
            Purchases.sharedInstance.logIn(currentUserUid) // RevenueCat
        }
    }
}