package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.tinygiants.getalife.domain.model.Header
import kotlin.random.Random

@Entity(tableName = "headers")
data class HeaderEntity(
    @PrimaryKey
    val id: Long = Random.nextLong(),
    val budgetId: Long = 0,
    val name: String,
    val isExpanded: Boolean = false
)

fun mapToHeaderEntity(header: Header) =
    HeaderEntity(
        id = header.id,
        name = header.name,
        isExpanded = header.isExpanded
    )

fun mapToHeaderEntity(name: String, isExpanded: Boolean) =
    HeaderEntity(
        name = name,
        isExpanded = isExpanded
    )