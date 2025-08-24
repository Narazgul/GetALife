package app.tinygiants.getalife.data.repository

import android.util.Log
import app.tinygiants.getalife.data.local.dao.BudgetDao
import app.tinygiants.getalife.data.local.entities.BudgetEntity
import app.tinygiants.getalife.data.remote.FirestoreDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Clock

/**
 * Repository for budget operations following Android's official offline-first architecture.
 * Room database is the single source of truth, with Firestore providing automatic sync.
 */
@Singleton
class BudgetRepository @Inject constructor(
    private val budgetDao: BudgetDao,
    private val firestoreDataSource: FirestoreDataSource,
    private val externalScope: CoroutineScope // App-scoped coroutine for background operations
) {
    /**
     * Get all budgets for a user - Room is the source of truth
     */
    fun getBudgetsFlow(firebaseUserId: String): Flow<List<BudgetEntity>> {
        return budgetDao.getBudgetsFlow(firebaseUserId)
    }

    /**
     * Get a specific budget by ID
     */
    suspend fun getBudget(budgetId: String): BudgetEntity? {
        return budgetDao.getBudget(budgetId)
    }

    /**
     * Get all budgets for a user (non-flow)
     */
    suspend fun getBudgets(firebaseUserId: String): List<BudgetEntity> {
        return budgetDao.getBudgets(firebaseUserId)
    }

    /**
     * Create a new budget and save to Firestore in background
     */
    suspend fun createBudget(name: String, firebaseUserId: String): BudgetEntity {
        val now = Clock.System.now()
        val budget = BudgetEntity(
            id = UUID.randomUUID().toString(),
            name = name,
            firebaseUserId = firebaseUserId,
            createdAt = now,
            lastModifiedAt = now,
            isSynced = false
        )
        
        // Save to local database first (offline-first principle)
        budgetDao.addBudget(budget)

        // Save to Firestore in background using external scope
        // This follows the official Android pattern for app-oriented operations
        externalScope.async {
            try {
                firestoreDataSource.saveBudget(budget)
                // Mark as synced in local database
                budgetDao.markBudgetAsSynced(budget.id, Clock.System.now().toEpochMilliseconds())
            } catch (e: Exception) {
                // Firestore will automatically retry when connection is restored
                // No need for explicit WorkManager since Firestore handles offline persistence
            }
        }
        
        return budget
    }

    /**
     * Update existing budget and sync to Firestore
     */
    suspend fun updateBudget(budget: BudgetEntity) {
        val updatedBudget = budget.copy(
            lastModifiedAt = Clock.System.now(),
            isSynced = false // Mark as needing sync
        )
        
        budgetDao.updateBudget(updatedBudget)

        // Background sync with Firestore
        externalScope.async {
            try {
                firestoreDataSource.saveBudget(updatedBudget)
                budgetDao.markBudgetAsSynced(updatedBudget.id, Clock.System.now().toEpochMilliseconds())
            } catch (e: Exception) {
                // Firestore handles offline persistence automatically
            }
        }
    }

    /**
     * Update Firebase User ID for existing budgets (handles anonymous -> authenticated transition)
     * This prevents creating duplicate budgets when Firebase user ID changes
     */
    suspend fun updateBudgetUserId(oldUserId: String, newUserId: String): Int {
        if (oldUserId == newUserId) return 0

        val budgetsToUpdate = budgetDao.getBudgets(oldUserId)
        Log.d("BudgetRepository", "Updating ${budgetsToUpdate.size} budgets from $oldUserId to $newUserId")

        budgetsToUpdate.forEach { budget ->
            val updatedBudget = budget.copy(
                firebaseUserId = newUserId,
                lastModifiedAt = Clock.System.now(),
                isSynced = false
            )
            updateBudget(updatedBudget)
        }

        return budgetsToUpdate.size
    }

    /**
     * Delete budget
     */
    suspend fun deleteBudget(budget: BudgetEntity) {
        budgetDao.deleteBudget(budget)
        // TODO: Handle deletion in Firestore
    }

    /**
     * Pull budgets from Firestore and merge with local data
     * This leverages Firestore's built-in conflict resolution
     */
    suspend fun syncWithFirestore(firebaseUserId: String) {
        try {
            val remoteBudgets = firestoreDataSource.getBudgets(firebaseUserId)
            val localBudgets = budgetDao.getBudgets(firebaseUserId)
            
            remoteBudgets.forEach { remoteBudget ->
                val localBudget = localBudgets.find { it.id == remoteBudget.id }
                
                if (localBudget == null) {
                    // New budget from server - add to local database
                    budgetDao.addBudget(remoteBudget)
                } else if (remoteBudget.lastModifiedAt > localBudget.lastModifiedAt) {
                    // Server version is newer - update local (simple conflict resolution)
                    budgetDao.updateBudget(remoteBudget)
                }
                // If local is newer, Firestore will automatically get the update on next sync
            }
        } catch (e: Exception) {
            // Handle network errors gracefully - local data remains source of truth
        }
    }

    /**
     * Handle account linking - merge anonymous user data with authenticated user
     * This method properly updates anonymous budgets to use the authenticated user ID
     */
    suspend fun linkAnonymousAccount(
        anonymousUserId: String, 
        authenticatedUserId: String,
        userName: String
    ) {
        Log.d("BudgetRepository", "Linking anonymous account $anonymousUserId to $authenticatedUserId")
        // Skip if same user ID
        if (anonymousUserId == authenticatedUserId) return

        // Get all budgets from anonymous user
        val anonymousBudgets = budgetDao.getBudgets(anonymousUserId)
        Log.d("BudgetRepository", "Found ${anonymousBudgets.size} anonymous budgets")

        if (anonymousBudgets.isEmpty()) {
            // No anonymous budgets to link - just ensure default budget exists
            val existingBudgets = budgetDao.getBudgets(authenticatedUserId)
            Log.d("BudgetRepository", "No anonymous budgets, found ${existingBudgets.size} existing authenticated budgets")
            if (existingBudgets.isEmpty()) {
                Log.d("BudgetRepository", "Creating default budget for authenticated user")
                createBudget(userName, authenticatedUserId)
            }
            return
        }

        // Get existing budgets for authenticated user (from remote/other devices)
        val existingAuthenticatedBudgets = try {
            firestoreDataSource.getBudgets(authenticatedUserId)
        } catch (e: Exception) {
            Log.w("BudgetRepository", "Failed to fetch remote budgets: ${e.message}")
            emptyList()
        }

        Log.d("BudgetRepository", "Found ${existingAuthenticatedBudgets.size} existing authenticated budgets from Firestore")

        if (existingAuthenticatedBudgets.isEmpty()) {
            // No existing authenticated budgets - simply update anonymous budgets to use new user ID
            Log.d("BudgetRepository", "No existing authenticated budgets, updating anonymous budgets to use new user ID")
            anonymousBudgets.forEach { anonymousBudget ->
                Log.d(
                    "BudgetRepository",
                    "Updating budget ${anonymousBudget.name} (${anonymousBudget.id}) from $anonymousUserId to $authenticatedUserId"
                )
                val linkedBudget = anonymousBudget.copy(
                    firebaseUserId = authenticatedUserId,
                    lastModifiedAt = Clock.System.now(),
                    isSynced = false // Mark for sync to Firestore
                )

                // Update in place - don't create new budget
                budgetDao.updateBudget(linkedBudget)

                // Sync to Firestore in background
                externalScope.async {
                    try {
                        firestoreDataSource.saveBudget(linkedBudget)
                        budgetDao.markBudgetAsSynced(linkedBudget.id, Clock.System.now().toEpochMilliseconds())
                        Log.d("BudgetRepository", "Successfully synced budget ${linkedBudget.id} to Firestore")
                    } catch (e: Exception) {
                        Log.w("BudgetRepository", "Failed to sync budget ${linkedBudget.id}: ${e.message}")
                        // Will retry when connection is available
                    }
                }
            }
        } else {
            // User has existing budgets from other devices
            Log.d("BudgetRepository", "User has existing budgets, merging anonymous budgets")
            // Add remote budgets to local database first
            existingAuthenticatedBudgets.forEach { remoteBudget ->
                val localBudget = budgetDao.getBudget(remoteBudget.id)
                if (localBudget == null) {
                    Log.d("BudgetRepository", "Adding remote budget ${remoteBudget.name} to local database")
                    budgetDao.addBudget(remoteBudget)
                }
            }

            // Then handle anonymous budgets - either merge or create with unique name
            anonymousBudgets.forEach { anonymousBudget ->
                val existingNames = existingAuthenticatedBudgets.map { it.name }
                val uniqueName = generateUniqueBudgetName(anonymousBudget.name, existingNames)
                Log.d("BudgetRepository", "Merging anonymous budget ${anonymousBudget.name} with unique name $uniqueName")

                val linkedBudget = anonymousBudget.copy(
                    name = uniqueName,
                    firebaseUserId = authenticatedUserId,
                    lastModifiedAt = Clock.System.now(),
                    isSynced = false
                )

                budgetDao.updateBudget(linkedBudget)

                // Sync to Firestore
                externalScope.async {
                    try {
                        firestoreDataSource.saveBudget(linkedBudget)
                        budgetDao.markBudgetAsSynced(linkedBudget.id, Clock.System.now().toEpochMilliseconds())
                        Log.d("BudgetRepository", "Successfully synced merged budget ${linkedBudget.id} to Firestore")
                    } catch (e: Exception) {
                        Log.w("BudgetRepository", "Failed to sync merged budget ${linkedBudget.id}: ${e.message}")
                        // Will retry later
                    }
                }
            }
        }

        // Clean up: Remove old anonymous budgets from database (they now have new user ID)
        // Note: We don't delete them, we just updated their firebaseUserId

        // Final sync to ensure consistency
        syncWithFirestore(authenticatedUserId)
        Log.d("BudgetRepository", "Finished linking anonymous account")
    }

    /**
     * Generate unique budget name with Roman numerals for duplicates
     */
    private fun generateUniqueBudgetName(baseName: String, existingNames: List<String>): String {
        if (baseName !in existingNames) return baseName
        
        val romanNumerals = listOf("II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X")
        return romanNumerals.firstNotNullOfOrNull { numeral ->
            val candidate = "$baseName $numeral"
            candidate.takeIf { it !in existingNames }
        } ?: "$baseName ${Clock.System.now().toEpochMilliseconds()}" // Fallback
    }
}