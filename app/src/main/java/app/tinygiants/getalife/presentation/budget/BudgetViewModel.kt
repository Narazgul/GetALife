package app.tinygiants.getalife.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.data.CategoryRepositoryImpl
import app.tinygiants.getalife.domain.model.CategoryHeader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val categoryRepository: CategoryRepositoryImpl
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetUiState(categories = emptyMap(), isLoading = true, errorMessage = null))
    val uiState = _uiState.asStateFlow()

    private var fetchJob: Job? = null

    // region Init

    init {
        loadCategories()
    }

    // endregion

    // region Private

    private fun loadCategories() {
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {

            delay(1000)
            val categoriesModel = categoryRepository.fetchCategories()
            mapToUiState(categoriesModel)
                .catch {
                    _uiState.value = BudgetUiState(
                        categories = emptyMap(),
                        isLoading = false,
                        errorMessage = ErrorMessage(
                            title =  "Zefix",
                            subtitle = "Ein fürchterlicher Fehler ist aufgetreten."
                        )
                    )
                }
                .collect { categories ->
                    _uiState.value =
                        BudgetUiState(
                            categories = categories,
                            isLoading = false,
                            errorMessage = null)
                }
        }
    }

    private fun mapToUiState(categoriesModel: Flow<Map<CategoryHeader, List<app.tinygiants.getalife.domain.model.Category>>>) =
        categoriesModel.map {
            it.map { (key, value) ->
                val headerKey = Header(
                    id = key.id,
                    name = key.name,
                    isExpanded = key.isExpanded,
                    toggleExpanded = {
                        viewModelScope.launch {
                            categoryRepository.toggleIsExpended(key.id)
                        }
                    }
                )
                val categoryValue = value.map { category ->
                    Category(
                        id = category.id,
                        name = category.name,
                        budgetTarget = category.budgetTarget,
                        availableMoney = category.availableMoney,
                        optionalText = category.optionalText
                    )
                }
                headerKey to categoryValue
            }.toMap()
        }

    // endregion
}