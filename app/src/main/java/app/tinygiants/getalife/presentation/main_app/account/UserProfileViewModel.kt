package app.tinygiants.getalife.presentation.main_app.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class UserProfileUiState(
    val user: FirebaseUser? = null,
    val displayName: String = "",
    val email: String = "",
    val isAnonymous: Boolean = true,
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val providers: List<String> = emptyList()
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val crashlytics: FirebaseCrashlytics
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            _uiState.value = _uiState.value.copy(
                user = user,
                displayName = user.displayName ?: "",
                email = user.email ?: "",
                isAnonymous = user.isAnonymous,
                providers = user.providerData.map { it.providerId }
            )
        }
    }

    fun toggleEditMode() {
        _uiState.value = _uiState.value.copy(
            isEditMode = !_uiState.value.isEditMode,
            errorMessage = null,
            successMessage = null
        )
    }

    fun updateDisplayName(name: String) {
        _uiState.value = _uiState.value.copy(displayName = name)
    }

    fun saveProfile() {
        val currentState = _uiState.value
        if (currentState.user == null) return

        _uiState.value = currentState.copy(isLoading = true)

        viewModelScope.launch {
            try {
                currentState.user.updateProfile(
                    userProfileChangeRequest {
                        displayName = currentState.displayName
                    }
                ).await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isEditMode = false,
                    successMessage = "Profil erfolgreich aktualisiert"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Fehler beim Aktualisieren des Profils: ${e.localizedMessage}"
                )
                crashlytics.recordException(e)
            }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        val user = firebaseAuth.currentUser
        if (user == null || user.email == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Kein authentifizierter Benutzer gefunden"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                // Re-authenticate user first
                val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                user.reauthenticate(credential).await()

                // Update password
                user.updatePassword(newPassword).await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Passwort erfolgreich geändert"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Fehler beim Ändern des Passworts: ${e.localizedMessage}"
                )
                crashlytics.recordException(e)
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                firebaseAuth.signOut()
                // AuthViewModel will handle creating a new anonymous user
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Fehler beim Abmelden: ${e.localizedMessage}"
                )
                crashlytics.recordException(e)
            }
        }
    }

    fun deleteAccount(currentPassword: String? = null) {
        val user = firebaseAuth.currentUser
        if (user == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Kein authentifizierter Benutzer gefunden"
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            try {
                // Re-authenticate if password is provided (for email users)
                if (currentPassword != null && user.email != null) {
                    val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
                    user.reauthenticate(credential).await()
                }

                // Delete the account
                user.delete().await()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Account erfolgreich gelöscht"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Fehler beim Löschen des Accounts: ${e.localizedMessage}"
                )
                crashlytics.recordException(e)
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    fun hasProvider(providerId: String): Boolean {
        return _uiState.value.providers.contains(providerId)
    }
}