package app.tinygiants.getalife.domain.usecase.budget

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case that provides the current budget ID for filtering data operations.
 * This ensures all data operations are scoped to the active budget.
 */
@Singleton
class GetCurrentBudgetUseCase @Inject constructor(
    private val budgetSelectionUseCase: BudgetSelectionUseCase
) {
    /**
     * Flow of the current budget ID. Emits null if no budget is selected.
     */
    val currentBudgetIdFlow: Flow<String?> = budgetSelectionUseCase.activeBudgetIdFlow

    /**
     * Flow that emits the current budget ID or a default value if none is selected.
     * This prevents null values in data operations.
     */
    val currentBudgetIdOrDefaultFlow: Flow<String> = budgetSelectionUseCase.activeBudgetIdFlow
        .map { budgetId -> budgetId ?: "default-budget" }

    /**
     * Get the current budget ID synchronously.
     * Returns null if no budget is selected.
     */
    suspend fun getCurrentBudgetId(): String? {
        return budgetSelectionUseCase.getActiveBudgetId()
    }

    /**
     * Get the current budget ID or create a default budget if none exists.
     * This method automatically handles the case when no budget is selected by creating
     * a default budget for new users, preventing the "No budget selected" error.
     */
    suspend fun requireCurrentBudgetId(): String {
        val currentBudgetId = getCurrentBudgetId()
        if (currentBudgetId != null) {
            return currentBudgetId
        }

        // Auto-initialize default budget for new users or when no budget is selected
        val defaultBudget = budgetSelectionUseCase.initializeDefaultBudget()
        return defaultBudget.id
    }

    /**
     * Get the current budget ID or return a default value.
     * Use this to prevent null values in data operations.
     */
    suspend fun getCurrentBudgetIdOrDefault(): String {
        return getCurrentBudgetId() ?: "default-budget"
    }
}