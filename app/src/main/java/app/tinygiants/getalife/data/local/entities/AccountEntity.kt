package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.tinygiants.getalife.domain.model.AccountType
import kotlinx.datetime.Instant

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: Long,
    val name: String,
    val balance: Double,
    val type: AccountType,
    val listPosition: Int,
    val updatedAt: Instant,
    val createdAt: Instant
)