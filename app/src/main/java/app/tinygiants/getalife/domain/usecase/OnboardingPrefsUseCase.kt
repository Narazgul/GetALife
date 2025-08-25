package app.tinygiants.getalife.domain.usecase

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Extension property for DataStore
private val Context.onboardingPrefs: DataStore<Preferences> by preferencesDataStore(name = "onboarding_preferences")

/**
 * Manages onboarding completion status using DataStore.
 * This tracks whether the user has completed the initial onboarding flow.
 */
@Singleton
class OnboardingPrefsUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.onboardingPrefs

    companion object {
        private val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
        private val TRANSACTION_ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("transaction_onboarding_completed")
    }

    val isOnboardingCompletedFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETED_KEY] ?: false
    }

    val isTransactionOnboardingCompletedFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[TRANSACTION_ONBOARDING_COMPLETED_KEY] ?: false
    }

    suspend fun markOnboardingCompleted() {
        dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED_KEY] = true
        }
    }

    suspend fun markTransactionOnboardingCompleted() {
        dataStore.edit { prefs ->
            prefs[TRANSACTION_ONBOARDING_COMPLETED_KEY] = true
        }
    }

    suspend fun resetOnboarding() {
        dataStore.edit { prefs ->
            prefs[ONBOARDING_COMPLETED_KEY] = false
        }
    }
}