package app.tinygiants.getalife.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userID: Long,
    @ColumnInfo(name = "username")
    val username: String?
)
