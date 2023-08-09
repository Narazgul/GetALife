package app.tinygiants.getalife.domain.model

data class CategoryHeader(
    val id: Int,
    val name: String = "kein Gruppentitel vergeben",
    val sumOfAvailableMoney: Double = 0.00,
    var isExpanded: Boolean = false
)