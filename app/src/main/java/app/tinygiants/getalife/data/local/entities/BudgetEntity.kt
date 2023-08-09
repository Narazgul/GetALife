package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String?,
    @Relation(
        parentColumn = "id",
        entityColumn = "budgetId"
    )
    val categoryGroups: List<CategoryGroupEntity>?
)