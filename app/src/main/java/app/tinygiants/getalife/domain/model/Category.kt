package app.tinygiants.getalife.domain.model

import kotlin.random.Random

data class Category(
    val id: Long = Random.nextLong(),
    val headerId: Long,
    val name: String = "kein Name vergeben",
    val budgetTarget: Double = 0.00,
    val availableMoney: Double = 0.00,
    val optionalText: String = ""
)