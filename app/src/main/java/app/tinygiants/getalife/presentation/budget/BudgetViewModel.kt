package app.tinygiants.getalife.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.data.CategoryRepositoryImpl
import app.tinygiants.getalife.domain.model.CategoryHeader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val categoryRepository: CategoryRepositoryImpl
) : ViewModel() {

    private val _budgetState = MutableStateFlow(BudgetState(categories = emptyMap(), isLoading = true, errorMessage = null))
    val uiState = _budgetState.asStateFlow()

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
            mapCategoryMapToUiState(categoriesModel)
                .catch {
                    _budgetState.value = BudgetState(
                        categories = emptyMap(),
                        isLoading = false,
                        errorMessage = it.localizedMessage
                    )
                }
                .collect { categories ->
                    _budgetState.value =
                        BudgetState(categories = categories, isLoading = false, errorMessage = null)
                }
        }
    }

    private fun mapCategoryMapToUiState(categoriesModel: Flow<Map<CategoryHeader, List<app.tinygiants.getalife.domain.model.Category>>>) =
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