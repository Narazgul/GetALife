package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.tinygiants.getalife.domain.model.Category
import kotlin.random.Random

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: Long = Random.nextLong(),
    val headerId: Long,
    val name: String,
    val budgetTarget: Double = 0.00,
    val availableMoney: Double = 0.00,
    val optionalText: String = ""
)

fun mapToCategoryEntity(category: Category) =
    CategoryEntity(
        id = category.id,
        headerId = category.headerId,
        name = category.name,
        budgetTarget = category.budgetTarget,
        availableMoney = category.availableMoney,
        optionalText = category.optionalText
    )

fun mapToCategoryEntity(headerId: Long, categoryName: String) =
    CategoryEntity(
        headerId = headerId,
        name = categoryName
    )