package app.tinygiants.getalife.domain.model

data class Category(
    val id: Int,
    val name: String = "kein Name vergeben",
    val budgetTarget: Double = 0.00,
    val availableMoney: Double = 0.00,
    val optionalText: String = ""
)