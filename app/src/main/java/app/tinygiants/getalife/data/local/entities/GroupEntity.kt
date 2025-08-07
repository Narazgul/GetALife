package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Group

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey
    val id: Long,
    val budgetId: String,
    val name: String,
    val listPosition: Int,
    val isExpanded: Boolean,
    val isSynced: Boolean = false // tracks if this group has been synced to Firestore
) {
    companion object {
        fun fromDomain(group: Group, budgetId: String): GroupEntity {
            return group.run {
                GroupEntity(
                    id = id,
                    budgetId = budgetId,
                    name = name,
                    listPosition = listPosition,
                    isExpanded = isExpanded,
                    isSynced = false // new groups are not synced initially
                )
            }
        }
    }

    fun toDomain(): Group {
        return Group(
            id = id,
            name = name,
            sumOfAvailableMoney = EmptyMoney(),
            listPosition = listPosition,
            isExpanded = isExpanded
        )
    }
}