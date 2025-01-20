package app.tinygiants.getalife.presentation.app_initialisation

import androidx.compose.runtime.Composable
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.crashlytics.crashlytics
import com.revenuecat.purchases.Purchases
import com.superwall.sdk.Superwall
import com.superwall.sdk.identity.identify

@Composable
fun InitAuthentication() {
    Firebase.auth.addAuthStateListener { auth ->

        val currentUser = auth.currentUser

        if (currentUser == null)
            auth.signInAnonymously()
                .addOnFailureListener { exception -> Firebase.crashlytics.recordException(exception) }
        else {
            Superwall.instance.identify(currentUser.uid)
            Purchases.sharedInstance.logIn(currentUser.uid)
        }
    }
}