package app.tinygiants.getalife.presentation.main_app.bulk_categorization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.categorization.BulkCategorizationResult
import app.tinygiants.getalife.domain.model.categorization.TransactionGroup
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.domain.repository.GroupRepository
import app.tinygiants.getalife.domain.usecase.categorization.BulkCategorizationUseCase
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Bulk Categorization Screen
 */
@HiltViewModel
class BulkCategorizationViewModel @Inject constructor(
    private val bulkCategorizationUseCase: BulkCategorizationUseCase,
    private val categoryRepository: CategoryRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BulkCategorizationUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadTransactionGroups()
        loadCategoriesAndGroups()
    }

    /**
     * Load uncategorized transaction groups
     */
    fun loadTransactionGroups() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val groups = bulkCategorizationUseCase.getUncategorizedTransactionGroups()

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        transactionGroups = groups,
                        hasData = groups.isNotEmpty()
                    )
                }
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Fehler beim Laden der Transaktionsgruppen: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Load available categories and groups for selection
     */
    private fun loadCategoriesAndGroups() {
        viewModelScope.launch {
            try {
                val categories = categoryRepository.getAllCategories()
                val groups = groupRepository.getAllGroups()

                _uiState.update {
                    it.copy(
                        availableCategories = categories,
                        availableGroups = groups
                    )
                }
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
            }
        }
    }

    /**
     * Apply categorization to a transaction group using existing category
     */
    fun categorizeGroupWithExistingCategory(group: TransactionGroup, categoryId: Long) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isProcessing = true) }

                val result = bulkCategorizationUseCase.categorizeBulkTransactions(
                    group = group,
                    categoryId = categoryId
                )

                when (result) {
                    is BulkCategorizationResult.Success -> {
                        _uiState.update { currentState ->
                            currentState.copy(
                                isProcessing = false,
                                transactionGroups = currentState.transactionGroups.filter { it.id != group.id },
                                successMessage = "✅ ${result.processedCount} Transaktionen erfolgreich kategorisiert",
                                hasData = currentState.transactionGroups.size > 1 // -1 because we removed the group
                            )
                        }

                        // Auto-dismiss success message after 3 seconds
                        kotlinx.coroutines.delay(3000)
                        clearMessages()
                    }

                    is BulkCategorizationResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                error = "Fehler bei der Kategorisierung: ${result.message}"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = "Unerwarteter Fehler: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Apply categorization to a transaction group with new category
     */
    fun categorizeGroupWithNewCategory(
        group: TransactionGroup,
        categoryName: String,
        groupId: Long
    ) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isProcessing = true) }

                val result = bulkCategorizationUseCase.categorizeBulkTransactions(
                    group = group,
                    newCategoryName = categoryName,
                    newCategoryGroupId = groupId
                )

                when (result) {
                    is BulkCategorizationResult.Success -> {
                        // Reload categories to include the new one
                        loadCategoriesAndGroups()

                        _uiState.update { currentState ->
                            currentState.copy(
                                isProcessing = false,
                                transactionGroups = currentState.transactionGroups.filter { it.id != group.id },
                                successMessage = "✅ Neue Kategorie '${result.category.name}' erstellt und ${result.processedCount} Transaktionen kategorisiert",
                                hasData = currentState.transactionGroups.size > 1
                            )
                        }

                        // Auto-dismiss success message after 4 seconds
                        kotlinx.coroutines.delay(4000)
                        clearMessages()
                    }

                    is BulkCategorizationResult.Error -> {
                        _uiState.update {
                            it.copy(
                                isProcessing = false,
                                error = "Fehler bei der Kategorisierung: ${result.message}"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Firebase.crashlytics.recordException(e)
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        error = "Unerwarteter Fehler: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Show categorization options for a group
     */
    fun showCategorizationOptions(group: TransactionGroup) {
        _uiState.update {
            it.copy(
                selectedGroup = group,
                showCategorizationDialog = true
            )
        }
    }

    /**
     * Hide categorization options dialog
     */
    fun hideCategorizationDialog() {
        _uiState.update {
            it.copy(
                selectedGroup = null,
                showCategorizationDialog = false
            )
        }
    }

    /**
     * Clear success and error messages
     */
    fun clearMessages() {
        _uiState.update {
            it.copy(
                successMessage = null,
                error = null
            )
        }
    }

    /**
     * Refresh data
     */
    fun refresh() {
        clearMessages()
        loadTransactionGroups()
        loadCategoriesAndGroups()
    }
}

/**
 * UI State for Bulk Categorization Screen
 */
data class BulkCategorizationUiState(
    val isLoading: Boolean = false,
    val isProcessing: Boolean = false,
    val transactionGroups: List<TransactionGroup> = emptyList(),
    val availableCategories: List<Category> = emptyList(),
    val availableGroups: List<app.tinygiants.getalife.domain.model.Group> = emptyList(),
    val selectedGroup: TransactionGroup? = null,
    val showCategorizationDialog: Boolean = false,
    val hasData: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)