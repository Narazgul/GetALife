package app.tinygiants.getalife.data.remote.dto

data class BudgetDto(
    val name: String? = null,
    val categories: List<CategoryDto>? = null
)

data class CategoryDto(
    val header: HeaderDto? = null,
    val items: List<ItemDto>? = null)

data class HeaderDto(
    val id: String? = null,
    val name: String? = null,
    @field:JvmField val isExpanded: Boolean? = null
)

data class ItemDto(
    val name: String? = null,
    val budgetTarget: Double? = null,
    val availableMoney: Double? = null
)