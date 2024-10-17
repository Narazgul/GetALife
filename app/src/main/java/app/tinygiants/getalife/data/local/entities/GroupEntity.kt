package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "headers")
data class GroupEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val listPosition: Int,
    val isExpanded: Boolean
)