package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.tinygiants.getalife.domain.model.EmptyMoney
import app.tinygiants.getalife.domain.model.Group

@Entity(tableName = "headers")
data class GroupEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val listPosition: Int,
    val isExpanded: Boolean
) {
    companion object {
        fun fromDomain(group: Group): GroupEntity {
            return group.run {
                GroupEntity(
                    id = id,
                    name = name,
                    listPosition = listPosition,
                    isExpanded = isExpanded
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