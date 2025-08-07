package app.tinygiants.getalife.data.repository

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
     */
    suspend fun linkAnonymousAccount(
        anonymousUserId: String, 
        authenticatedUserId: String,
        userName: String
    ) {
        // Get all budgets from anonymous user
        val anonymousBudgets = budgetDao.getBudgets(anonymousUserId)
        
        if (anonymousBudgets.isNotEmpty()) {
            // First, try to get existing remote budgets
            val existingRemoteBudgets = try {
                firestoreDataSource.getBudgets(authenticatedUserId)
            } catch (e: Exception) {
                emptyList()
            }
            
            if (existingRemoteBudgets.isEmpty()) {
                // No existing online budgets - link anonymous budgets to authenticated user
                anonymousBudgets.forEach { budget ->
                    val linkedBudget = budget.copy(
                        firebaseUserId = authenticatedUserId,
                        lastModifiedAt = Clock.System.now(),
                        isSynced = false
                    )
                    budgetDao.updateBudget(linkedBudget)

                    // Save to Firestore in background
                    externalScope.async {
                        try {
                            firestoreDataSource.saveBudget(linkedBudget)
                            budgetDao.markBudgetAsSynced(linkedBudget.id, Clock.System.now().toEpochMilliseconds())
                        } catch (e: Exception) {
                            // Will sync later when connection is available
                        }
                    }
                }
            } else {
                // User has existing online budgets - create new budget with unique name
                val newBudgetName = generateUniqueBudgetName(userName, existingRemoteBudgets.map { it.name })
                val mergedBudget = anonymousBudgets.first().copy(
                    name = newBudgetName,
                    firebaseUserId = authenticatedUserId,
                    lastModifiedAt = Clock.System.now(),
                    isSynced = false
                )
                budgetDao.updateBudget(mergedBudget)

                // Also add existing remote budgets to local database
                existingRemoteBudgets.forEach { remoteBudget ->
                    budgetDao.addBudget(remoteBudget)
                }
            }
        }

        // Trigger a full sync
        syncWithFirestore(authenticatedUserId)
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