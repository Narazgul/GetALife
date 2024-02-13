package app.tinygiants.getalife.presentation.budget

import androidx.compose.runtime.Immutable
import app.tinygiants.getalife.domain.usecase.CurrencyUseCase
import app.tinygiants.getalife.domain.usecase.toCurrencyFormattedString

@Immutable
data class BudgetUiState(
    val groups: Map<UiHeader, List<UiCategory>>,
    val isLoading: Boolean,
    val errorMessage: ErrorMessage?
)

@Immutable
data class ErrorMessage(
    val title: String?,
    val subtitle: String?
)

@Immutable
data class UiHeader(
    val id: Long,
    val name: String,
    val sumOfAvailableMoney: Money,
    var isExpanded: Boolean = false
)

@Immutable
data class UiCategory(
    val id: Long,
    val headerId: Long,
    val name: String,
    val budgetTarget: Money,
    val availableMoney: Money,
    var progress: Float,
    val optionalText: String,
)

@Immutable
data class Money(
    val value: Double,
    val currencyCode: String = CurrencyUseCase.getCurrencyCode(),
    val currencySymbol: String = CurrencyUseCase.getCurrencySymbol(),
    val formattedMoney: String = value.toCurrencyFormattedString()
)

sealed class UserClickEvent {
    data class ToggleCategoryGroupExpandedState(val header: UiHeader): UserClickEvent()
    data class AddHeader(val name: String): UserClickEvent()
    data class UpdateHeaderName(val header: UiHeader): UserClickEvent()
    data class DeleteHeader(val header: UiHeader): UserClickEvent()

    data class AddCategory(val headerId: Long, val categoryName: String): UserClickEvent()
    data class UpdateCategory(val category: UiCategory): UserClickEvent()
    data class DeleteCategory(val category: UiCategory): UserClickEvent()
}