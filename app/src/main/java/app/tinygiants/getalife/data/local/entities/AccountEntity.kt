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
    val isClosed: Boolean = false,
    val updatedAt: Instant,
    val createdAt: Instant,
    val budgetId: String,
    val isSynced: Boolean = false // tracks if this account has been synced to Firestore
) {
    companion object {
        fun fromDomain(account: Account, budgetId: String): AccountEntity {
            return account.run {
                AccountEntity(
                    id = id,
                    name = name,
                    balance = balance.asDouble(),
                    type = type,
                    listPosition = listPosition,
                    isClosed = isClosed,
                    updatedAt = updatedAt,
                    createdAt = createdAt,
                    budgetId = budgetId,
                    isSynced = false // new accounts are not synced initially
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
            isClosed = isClosed,
            updatedAt = updatedAt,
            createdAt = createdAt
        )
    }
}