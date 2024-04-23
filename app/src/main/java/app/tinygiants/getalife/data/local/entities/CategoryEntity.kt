package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: Long,
    val headerId: Long,
    val emoji: String,
    val name: String,
    val budgetTarget: Double,
    val availableMoney: Double,
    val optionalText: String,
    val listPosition: Int,
    val isEmptyCategory: Boolean
)