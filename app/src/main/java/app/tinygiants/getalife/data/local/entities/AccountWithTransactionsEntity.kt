package app.tinygiants.getalife.data.local.entities

import androidx.room.Embedded
import androidx.room.Relation

data class AccountWithTransactionsEntity(
    @Embedded val account: AccountEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "accountId"
    )
    val transactions: List<TransactionEntity>
)