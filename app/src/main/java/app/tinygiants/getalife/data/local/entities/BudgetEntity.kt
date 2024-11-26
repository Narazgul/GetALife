package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.Instant

const val DEFAULT_BUDGET_ID = 1L

@Entity(tableName = "budget")
data class BudgetEntity(
    @PrimaryKey
    val id: Long = DEFAULT_BUDGET_ID,
    val readyToAssign: Double,
    val updatedAt: Instant,
    val createdAt: Instant
)