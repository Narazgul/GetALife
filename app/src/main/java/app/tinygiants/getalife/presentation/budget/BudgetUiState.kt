package app.tinygiants.getalife.presentation.budget

import app.tinygiants.getalife.util.toCurrencyFormattedString

data class BudgetUiState(
    val categories: Map<Header, List<Category>>,
    val isLoading: Boolean = false,
    val errorMessage: ErrorMessage? = null
)

data class ErrorMessage(
    val title: String?,
    val subtitle: String?
)

data class Header(
    val id: Int,
    val name: String,
    val sumOfAvailableMoney: Money,
    var isExpanded: Boolean = false,
    val toggleExpanded: () -> Unit
)

data class Category(
    val id: Int,
    val name: String,
    val budgetTarget: Money,
    val availableMoney: Money,
    var progress: Float,
    val optionalText: String,
)

data class Money(
    val value: Double,
    val formattedMoney: String = value.toCurrencyFormattedString()
)