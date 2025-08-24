package app.tinygiants.getalife.domain.usecase.budget.initialization

import android.util.Log
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.data.local.entities.BudgetEntity
import app.tinygiants.getalife.data.repository.BudgetRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case responsible for initializing default budgets for new users.
 * Handles Firebase user ID changes from anonymous to authenticated.
 */
class InitializeBudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val firebaseAuth: FirebaseAuth,
    @Default private val defaultDispatcher: CoroutineDispatcher
) {
    private val budgetMutex = Mutex()

    /**
     * Initialize default budget for new users.
     * Returns the initialized or existing budget.
     */
    suspend operator fun invoke(): BudgetEntity = withContext(defaultDispatcher) {
        val currentUserId = getCurrentFirebaseUserId()
        val userName = firebaseAuth.currentUser?.displayName ?: "Mein Budget"

        Log.d("InitializeBudgetUseCase", "Initializing default budget for user $currentUserId")

        budgetMutex.withLock {
            // Check for existing budgets with current user ID
            var existingBudgets = budgetRepository.getBudgets(currentUserId)
            Log.d("InitializeBudgetUseCase", "Found ${existingBudgets.size} existing budgets for user $currentUserId")

            // If no budgets for current user ID, check if we have anonymous budgets that need updating
            if (existingBudgets.isEmpty() && currentUserId != "anonymous") {
                Log.d("InitializeBudgetUseCase", "No budgets for current user, checking for anonymous budgets to update")

                val updatedCount = budgetRepository.updateBudgetUserId("anonymous", currentUserId)
                if (updatedCount > 0) {
                    Log.d("InitializeBudgetUseCase", "Successfully updated $updatedCount anonymous budgets to $currentUserId")
                    // Refresh the existing budgets list
                    existingBudgets = budgetRepository.getBudgets(currentUserId)
                }
            }

            if (existingBudgets.isEmpty()) {
                // Create first budget only if we truly have no budgets
                Log.d("InitializeBudgetUseCase", "No existing budgets found after checking anonymous, creating first budget")
                budgetRepository.createBudget(userName, currentUserId)
            } else {
                Log.d("InitializeBudgetUseCase", "Using existing budget: ${existingBudgets.first().name}")
                existingBudgets.first()
            }
        }
    }

    private fun getCurrentFirebaseUserId(): String {
        return firebaseAuth.currentUser?.uid ?: "anonymous"
    }
}