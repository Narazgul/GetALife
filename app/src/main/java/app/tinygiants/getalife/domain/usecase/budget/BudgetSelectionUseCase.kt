package app.tinygiants.getalife.domain.usecase.budget

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.tinygiants.getalife.data.local.entities.BudgetEntity
import app.tinygiants.getalife.data.repository.BudgetRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

// Extension property for DataStore
private val Context.budgetPrefs: DataStore<Preferences> by preferencesDataStore(name = "budget_preferences")

/**
 * Use case for managing active budget selection and Firebase user operations.
 * Handles budget switching, account linking, and automatic budget initialization.
 */
@Singleton
class BudgetSelectionUseCase @Inject constructor(
    private val context: Context,
    private val budgetRepository: BudgetRepository,
    private val firebaseAuth: FirebaseAuth
) {
    private val activeBudgetIdKey = stringPreferencesKey("active_budget_id")
    private val dataStore = context.budgetPrefs
    private val budgetMutex = Mutex()

    /**
     * Flow of the currently active budget ID
     */
    val activeBudgetIdFlow: Flow<String?> = dataStore.data.map { prefs ->
        prefs[activeBudgetIdKey]
    }

    /**
     * Get the current active budget ID
     */
    suspend fun getActiveBudgetId(): String? {
        return activeBudgetIdFlow.first()
    }

    /**
     * Set the active budget ID
     */
    suspend fun setActiveBudgetId(budgetId: String) {
        dataStore.edit { prefs ->
            prefs[activeBudgetIdKey] = budgetId
        }
    }

    /**
     * Get all budgets for the current user
     */
    fun getBudgetsFlow(): Flow<List<BudgetEntity>> {
        val currentUserId = getCurrentFirebaseUserId()
        return budgetRepository.getBudgetsFlow(currentUserId)
    }

    /**
     * Get the active budget entity
     */
    suspend fun getActiveBudget(): BudgetEntity? {
        val activeBudgetId = getActiveBudgetId() ?: return null
        return budgetRepository.getBudget(activeBudgetId)
    }

    /**
     * Create a new budget and optionally set it as active
     */
    suspend fun createBudget(name: String, setAsActive: Boolean = true): BudgetEntity {
        val currentUserId = getCurrentFirebaseUserId()
        val budget = budgetRepository.createBudget(name, currentUserId)

        if (setAsActive) {
            setActiveBudgetId(budget.id)
        }

        Log.d("BudgetSelectionUseCase", "Budget created with name $name and id ${budget.id}")
        return budget
    }

    /**
     * Initialize default budget for new users
     * IMPORTANT: Handles Firebase user ID changes from anonymous to authenticated
     */
    suspend fun initializeDefaultBudget(): BudgetEntity {
        val currentUserId = getCurrentFirebaseUserId()
        val userName = firebaseAuth.currentUser?.displayName ?: "Mein Budget"

        Log.d("BudgetSelectionUseCase", "Initializing default budget for user $currentUserId")

        return budgetMutex.withLock {
            // Check for existing budgets with current user ID
            var existingBudgets = budgetRepository.getBudgets(currentUserId)
            Log.d("BudgetSelectionUseCase", "Found ${existingBudgets.size} existing budgets for user $currentUserId")

            // If no budgets for current user ID, check if we have anonymous budgets that need updating
            if (existingBudgets.isEmpty() && currentUserId != "anonymous") {
                Log.d("BudgetSelectionUseCase", "No budgets for current user, checking for anonymous budgets to update")

                val updatedCount = budgetRepository.updateBudgetUserId("anonymous", currentUserId)
                if (updatedCount > 0) {
                    Log.d("BudgetSelectionUseCase", "Successfully updated $updatedCount anonymous budgets to $currentUserId")
                    // Refresh the existing budgets list
                    existingBudgets = budgetRepository.getBudgets(currentUserId)
                }
            }

            if (existingBudgets.isEmpty()) {
                // Create first budget only if we truly have no budgets
                Log.d("BudgetSelectionUseCase", "No existing budgets found after checking anonymous, creating first budget")
                return createBudget(userName, setAsActive = true)
            } else {
                // Set first budget as active if none is selected
                val activeBudgetId = getActiveBudgetId()
                Log.d("BudgetSelectionUseCase", "Existing budgets found, active budget ID: $activeBudgetId")
                if (activeBudgetId == null || !existingBudgets.any { it.id == activeBudgetId }) {
                    Log.d(
                        "BudgetSelectionUseCase",
                        "No valid active budget set, setting first budget as active: ${existingBudgets.first().id}"
                    )
                    setActiveBudgetId(existingBudgets.first().id)
                }
                return existingBudgets.first()
            }
        }
    }

    /**
     * Handle Firebase account linking from anonymous to authenticated
     */
    suspend fun handleAccountLinking(authenticatedUserId: String, previousAnonymousUserId: String? = null) {
        val anonymousUserId = previousAnonymousUserId ?: getCurrentFirebaseUserId()
        val userName = firebaseAuth.currentUser?.displayName ?: "Mein Budget"

        // Only link if we have a valid anonymous user ID and it's different from authenticated ID
        if (anonymousUserId != authenticatedUserId && anonymousUserId != "anonymous") {
            // Link anonymous account data using the new UseCase signature
            budgetRepository.linkAnonymousAccount(
                anonymousUserId = anonymousUserId,
                authenticatedUserId = authenticatedUserId,
                userName = userName
            )
        }

        // Ensure we have an active budget for the authenticated user
        initializeDefaultBudget()
    }

    /**
     * Switch to a different budget
     */
    suspend fun switchToBudget(budgetId: String) {
        val budget = budgetRepository.getBudget(budgetId)
        if (budget != null) {
            setActiveBudgetId(budgetId)
        }
    }

    /**
     * Get the current Firebase user ID (handles anonymous users)
     */
    fun getCurrentFirebaseUserId(): String {
        return firebaseAuth.currentUser?.uid ?: "anonymous"
    }

    /**
     * Check if current user is anonymous
     */
    fun isAnonymousUser(): Boolean {
        return firebaseAuth.currentUser?.isAnonymous == true
    }

    /**
     * Get display name for current user
     */
    fun getCurrentUserDisplayName(): String {
        return firebaseAuth.currentUser?.displayName
            ?: firebaseAuth.currentUser?.email
            ?: "Unbekannter Nutzer"
    }
}