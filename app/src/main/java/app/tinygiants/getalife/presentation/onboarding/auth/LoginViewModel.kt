package app.tinygiants.getalife.presentation.onboarding.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.TwitterAuthProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val loginSuccess: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val crashlytics: FirebaseCrashlytics
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            emailError = null
        )
    }

    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(
            password = password,
            passwordError = null
        )
    }

    fun signInWithEmail() {
        val currentState = _uiState.value

        // Validate inputs
        val emailError = validateEmail(currentState.email)
        val passwordError = validatePassword(currentState.password)

        if (emailError != null || passwordError != null) {
            _uiState.value = currentState.copy(
                emailError = emailError,
                passwordError = passwordError
            )
            return
        }

        _uiState.value = currentState.copy(isLoading = true)

        viewModelScope.launch {
            try {
                firebaseAuth.signInWithEmailAndPassword(
                    currentState.email.trim(),
                    currentState.password
                ).await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    loginSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = getErrorMessage(e)
                )
                crashlytics.recordException(e)
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(idToken, null)
                firebaseAuth.signInWithCredential(credential).await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    loginSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Google Sign-In fehlgeschlagen: ${e.localizedMessage}"
                )
                crashlytics.recordException(e)
            }
        }
    }

    fun signInWithFacebook(accessToken: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val credential = FacebookAuthProvider.getCredential(accessToken)
                firebaseAuth.signInWithCredential(credential).await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    loginSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Facebook Sign-In fehlgeschlagen: ${e.localizedMessage}"
                )
                crashlytics.recordException(e)
            }
        }
    }

    fun signInWithTwitter(token: String, secret: String) {
        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val credential = TwitterAuthProvider.getCredential(token, secret)
                firebaseAuth.signInWithCredential(credential).await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    loginSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Twitter Sign-In fehlgeschlagen: ${e.localizedMessage}"
                )
                crashlytics.recordException(e)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    private fun validateEmail(email: String): String? {
        return when {
            email.isBlank() -> "E-Mail ist erforderlich"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Ungültige E-Mail-Adresse"
            else -> null
        }
    }

    private fun validatePassword(password: String): String? {
        return when {
            password.isBlank() -> "Passwort ist erforderlich"
            password.length < 6 -> "Passwort muss mindestens 6 Zeichen haben"
            else -> null
        }
    }

    private fun getErrorMessage(exception: Exception): String {
        return when (exception) {
            is FirebaseAuthInvalidUserException -> "Kein Benutzer mit dieser E-Mail gefunden"
            is FirebaseAuthInvalidCredentialsException -> "Ungültige E-Mail oder Passwort"
            is FirebaseAuthWeakPasswordException -> "Passwort ist zu schwach"
            else -> "Anmeldung fehlgeschlagen: ${exception.localizedMessage}"
        }
    }
}