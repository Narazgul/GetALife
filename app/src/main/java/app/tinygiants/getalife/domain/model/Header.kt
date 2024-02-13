package app.tinygiants.getalife.domain.model

import kotlin.random.Random

data class Header(
    val id: Long = Random.nextLong(),
    val name: String = "kein Gruppentitel vergeben",
    val availableMoney: Double = 0.00,
    var isExpanded: Boolean = false
)