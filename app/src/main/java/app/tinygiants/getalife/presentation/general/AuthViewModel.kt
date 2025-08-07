package app.tinygiants.getalife.presentation.general

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.domain.usecase.user.LinkAnonymousUserUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.revenuecat.purchases.Purchases
import com.superwall.sdk.Superwall
import com.superwall.sdk.identity.identify
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val purchases: Purchases,
    private val superwall: Superwall,
    private val crashlytics: FirebaseCrashlytics,
    private val linkAnonymousUserUseCase: LinkAnonymousUserUseCase
) : ViewModel() {

    private var previousUserId: String? = null

    init {
        handleAuthentication()
    }

    fun handleAuthentication() {
        firebaseAuth.addAuthStateListener { auth ->
            val currentUser = auth.currentUser
            if (currentUser == null) {
                signInAnonymously()
            } else {
                val currentUserId = currentUser.uid
                val wasAnonymous = previousUserId != null && previousUserId != currentUserId

                processAuthenticatedUser(currentUserId, wasAnonymous)

                // Update the previous user ID for the next state change
                previousUserId = currentUserId
            }
        }
    }

    private fun signInAnonymously() {
        viewModelScope.launch {
            try {
                firebaseAuth.signInAnonymously().await()
            } catch (e: Exception) {
                crashlytics.recordException(e)
            }
        }
    }

    private fun processAuthenticatedUser(userId: String, wasAnonymous: Boolean) {
        crashlytics.setUserId(userId)

        // Identify with Superwall and RevenueCat
        try {
            superwall.identify(userId)
            purchases.logIn(userId)
        } catch (e: Exception) {
            crashlytics.recordException(e)
        }

        if (wasAnonymous) {
            viewModelScope.launch {
                try {
                    linkAnonymousUserUseCase(userId)
                } catch (e: Exception) {
                    crashlytics.recordException(e)
                }
            }
        }
    }
}