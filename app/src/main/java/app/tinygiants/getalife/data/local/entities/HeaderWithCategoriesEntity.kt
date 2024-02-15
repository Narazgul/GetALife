package app.tinygiants.getalife.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class HeaderWithCategoriesEntity(
    @Embedded val header: HeaderEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "headerId"
    )
    val categories: List<CategoryEntity>
)