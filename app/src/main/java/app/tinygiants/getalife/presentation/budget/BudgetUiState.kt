package app.tinygiants.getalife.presentation.budget

data class BudgetUiState(
    val categories: Map<Header, List<Category>>,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

data class Header(
    val id: Int,
    val name: String,
    var isExpanded: Boolean = false,
    val toggleExpanded: () -> Unit
)

data class Category(
    val id: Int,
    val name: String,
    val budgetTarget: Double,
    val availableMoney: Double,
    val optionalText: String,
)