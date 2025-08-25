package app.tinygiants.getalife.presentation.main_app.transaction.add_transaction

import androidx.compose.runtime.Immutable
import app.tinygiants.getalife.domain.model.categorization.CategorizationResult

/**
 * UI State for smart categorization feature
 */
@Immutable
data class SmartCategorizationUiState(
    val isLoading: Boolean = false,
    val categorizationResult: CategorizationResult? = null,
    val showBottomSheet: Boolean = false,
    val error: String? = null
) {
    val hasValidSuggestion: Boolean
        get() = categorizationResult?.hasAnyMatch == true
}