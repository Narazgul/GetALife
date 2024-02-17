package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "headers")
data class HeaderEntity(
    @PrimaryKey
    val id: Long,
    val budgetId: Long = 0,
    val name: String,
    val listPosition: Int,
    val isExpanded: Boolean
)