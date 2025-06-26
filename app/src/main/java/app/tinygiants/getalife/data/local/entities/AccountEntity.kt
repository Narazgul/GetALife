package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.tinygiants.getalife.domain.model.Account
import app.tinygiants.getalife.domain.model.AccountType
import app.tinygiants.getalife.domain.model.Money
import kotlin.time.Instant

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
) {
    companion object {
        fun fromDomain(account: Account): AccountEntity {
            return account.run {
                AccountEntity(
                    id = id,
                    name = name,
                    balance = balance.asDouble(),
                    type = type,
                    listPosition = listPosition,
                    updatedAt = updatedAt,
                    createdAt = createdAt
                )
            }
        }
    }

    fun toDomain(): Account {
        return Account(
            id = id,
            name = name,
            balance = Money(value = balance),
            type = type,
            listPosition = listPosition,
            updatedAt = updatedAt,
            createdAt = createdAt
        )
    }
}