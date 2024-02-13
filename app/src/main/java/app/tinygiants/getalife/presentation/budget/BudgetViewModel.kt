package app.tinygiants.getalife.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.tinygiants.getalife.di.Default
import app.tinygiants.getalife.domain.model.Category
import app.tinygiants.getalife.domain.model.Group
import app.tinygiants.getalife.domain.model.Header
import app.tinygiants.getalife.domain.repository.CategoryRepository
import app.tinygiants.getalife.presentation.budget.UserClickEvent.AddCategory
import app.tinygiants.getalife.presentation.budget.UserClickEvent.AddHeader
import app.tinygiants.getalife.presentation.budget.UserClickEvent.DeleteCategory
import app.tinygiants.getalife.presentation.budget.UserClickEvent.DeleteHeader
import app.tinygiants.getalife.presentation.budget.UserClickEvent.ToggleCategoryGroupExpandedState
import app.tinygiants.getalife.presentation.budget.UserClickEvent.UpdateCategory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository,
    @Default private val defaultDispatcher: CoroutineDispatcher,
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
            launch {

                delay(1000L)

                categoryRepository.getBudget()
                    .catch { throwable -> displayErrorState(throwable) }
                    .collect { result ->
                        result.onSuccess { budgetListElements -> displayBudgetList(budgetListElements) }
                        result.onFailure { throwable -> displayErrorState(throwable) }
                    }
            }
        }
    }

    // endregion

    // region User interaction

    fun onUserClickEvent(clickEvent: UserClickEvent) {
        viewModelScope.launch {
            when (clickEvent) {

                is ToggleCategoryGroupExpandedState -> categoryRepository.toggleIsExpanded(
                    header = Header(
                        id = clickEvent.header.id,
                        name = clickEvent.header.name,
                        availableMoney = clickEvent.header.sumOfAvailableMoney.value,
                        isExpanded = clickEvent.header.isExpanded
                    )
                )

                is AddHeader -> categoryRepository.addHeader(name = clickEvent.name)
                is AddCategory -> categoryRepository.addCategory(
                    headerId = clickEvent.headerId,
                    categoryName = clickEvent.categoryName
                )

                is UserClickEvent.UpdateHeaderName -> categoryRepository.updateHeader(
                    Header(
                        id = clickEvent.header.id,
                        name = clickEvent.header.name,
                        isExpanded = clickEvent.header.isExpanded
                    )
                )

                is DeleteHeader -> categoryRepository.deleteHeader(
                    Header(id = clickEvent.header.id)
                )

                is UpdateCategory -> categoryRepository.updateCategory(
                    category = Category(
                        id = clickEvent.category.id,
                        headerId = clickEvent.category.headerId,
                        name = clickEvent.category.name,
                        budgetTarget = clickEvent.category.budgetTarget.value,
                        availableMoney = clickEvent.category.availableMoney.value
                    )
                )

                is DeleteCategory -> categoryRepository.deleteCategory(
                    category = Category(
                        id = clickEvent.category.id,
                        headerId = clickEvent.category.headerId,
                        name =  clickEvent.category.name,
                        budgetTarget = clickEvent.category.budgetTarget.value,
                        availableMoney = clickEvent.category.availableMoney.value
                    )
                )
            }
        }
    }

    // endregion

    // region Private Helper functions

    private suspend fun displayBudgetList(groups: List<Group>) {

        val budgetList = groups.associate { budgetListElement ->
            val header = mapToUiCategoryGroupHeader(budgetListElement)
            val items = mapToUiCategories(budgetListElement)

            header to items
        }

        _uiState.update {
            BudgetUiState(
                groups = budgetList,
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
                    subtitle = exception?.message
                        ?: "Ein fürchterlicher Fehler ist aufgetreten."
                )
            )
        }
    }

    private suspend fun mapToUiCategoryGroupHeader(group: Group): UiHeader {
        return withContext(defaultDispatcher) {

            val header = group.header
            val sumOfAvailableMoneyInCategory =
                group.categories.sumOf { category -> category.availableMoney }

            UiHeader(
                id = header.id,
                name = header.name,
                sumOfAvailableMoney = Money(value = sumOfAvailableMoneyInCategory),
                isExpanded = header.isExpanded
            )
        }
    }

    private suspend fun mapToUiCategories(group: Group): List<UiCategory> {
        return withContext(defaultDispatcher) {

            group.categories.map { category ->
                val progress = (category.availableMoney / category.budgetTarget).toFloat()

                UiCategory(
                    id = category.id,
                    headerId = group.header.id,
                    name = category.name,
                    budgetTarget = Money(value = category.budgetTarget),
                    availableMoney = Money(value = category.availableMoney),
                    progress = progress,
                    optionalText = category.optionalText
                )
            }
        }
    }

    // endregion
}