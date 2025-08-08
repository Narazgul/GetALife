package app.tinygiants.getalife.presentation.general

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.domain.usecase.user.LinkAnonymousUserUseCase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.revenuecat.purchases.Purchases
import com.superwall.sdk.Superwall
import com.superwall.sdk.identity.identify
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@Composable
fun Authentication(
    viewModel: AuthViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.handleAuthentication()
    }
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val purchases: Purchases,
    private val superwall: Superwall,
    private val crashlytics: FirebaseCrashlytics,
    private val linkAnonymousUserUseCase: LinkAnonymousUserUseCase
) : ViewModel() {

    private var previousUserId: String? = null

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    private val _currentUser = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        handleAuthentication()
    }

    fun handleAuthentication() {
        firebaseAuth.addAuthStateListener { auth ->
            val currentUser = auth.currentUser

            _currentUser.value = currentUser
            _isAuthenticated.value = currentUser != null && !currentUser.isAnonymous

            if (currentUser == null) {
                // No user signed in, create anonymous user
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

    fun signOut() {
        viewModelScope.launch {
            try {
                firebaseAuth.signOut()
                // After sign out, the auth state listener will automatically create a new anonymous user
            } catch (e: Exception) {
                crashlytics.recordException(e)
            }
        }
    }

    fun isUserAuthenticated(): Boolean {
        val user = firebaseAuth.currentUser
        return user != null && !user.isAnonymous
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

        // If user was previously anonymous and now has a full account, link the data
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

