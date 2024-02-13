package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val budgetName: String,
    val ownerId: Long
)