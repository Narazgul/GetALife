package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "category_groups")
data class CategoryGroupEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val headerId: Int,
    val budgetId: Int,
    @Relation(
        parentColumn = "id",
        entityColumn = "groupId"
    )
    val items: List<ItemEntity>?
)
