package app.tinygiants.getalife.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Header
import app.tinygiants.getalife.domain.usecase.GetBudgetUseCase
import app.tinygiants.getalife.domain.usecase.category.AddCategoryUseCase
import app.tinygiants.getalife.domain.usecase.category.DeleteCategoryUseCase
import app.tinygiants.getalife.domain.usecase.category.UpdateCategoryUseCase
import app.tinygiants.getalife.domain.usecase.header.AddHeaderUseCase
import app.tinygiants.getalife.domain.usecase.header.DeleteHeaderUseCase
import app.tinygiants.getalife.domain.usecase.header.UpdateHeaderUseCase
import app.tinygiants.getalife.presentation.budget.UserClickEvent.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val getBudget: GetBudgetUseCase,
    private val addHeader: AddHeaderUseCase,
    private val updateHeader: UpdateHeaderUseCase,
    private val deleteHeader: DeleteHeaderUseCase,
    private val addCategory: AddCategoryUseCase,
    private val updateCategory: UpdateCategoryUseCase,
    private val deleteCategory: DeleteCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        BudgetUiState(
            groups = emptyMap(),
            isLoading = true,
            errorMessage = null
        )
    )
    val uiState = _uiState.asStateFlow()

    // region Init

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            getBudget()
                .catch { throwable -> displayErrorState(throwable) }
                .collect { result ->
                    result.onSuccess { budgetListElements -> displayBudgetList(budgetListElements) }
                    result.onFailure { throwable -> displayErrorState(throwable) }
                }
        }
    }

    // endregion

    // region User interaction

    fun onUserClickEvent(clickEvent: UserClickEvent) {
        viewModelScope.launch {
            when (clickEvent) {

                is AddHeader      -> addHeader(headerName = clickEvent.name)
                is UpdateHeader   -> updateHeader(header = clickEvent.header)
                is DeleteHeader   -> deleteHeader(header = clickEvent.header)

                is ReplaceEmptyCategory -> updateCategory(clickEvent.category)

                is AddCategory    -> addCategory(headerId = clickEvent.headerId, categoryName = clickEvent.categoryName)
                is UpdateCategory -> updateCategory(category = clickEvent.category)
                is DeleteCategory -> deleteCategory(category = clickEvent.category)
            }
        }
    }

    // endregion

    // region Private Helper functions

    private fun displayBudgetList(groups: Map<Header, List<Category>>) {
        _uiState.update {
            BudgetUiState(
                groups = groups,
                isLoading = false,
                errorMessage = null
            )
        }
    }

    private fun displayErrorState(exception: Throwable?) {
        _uiState.update {
            BudgetUiState(
                groups = emptyMap(),
                isLoading = false,
                errorMessage = ErrorMessage(
                    title = "Zefix",
                    subtitle = exception?.message ?: "Ein fürchterlicher Fehler ist aufgetreten."
                )
            )
        }
    }

    // endregion
}