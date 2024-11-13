package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.tinygiants.getalife.domain.model.BudgetPurpose
import kotlinx.datetime.Instant

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: Long,
    val groupId: Long,
    val emoji: String,
    val name: String,
    val budgetTarget: Double?,
    val budgetPurpose: BudgetPurpose,
    val assignedMoney: Double,
    val availableMoney: Double,
    val listPosition: Int,
    val isInitialCategory: Boolean,
    val updatedAt: Instant,
    val createdAt: Instant
)