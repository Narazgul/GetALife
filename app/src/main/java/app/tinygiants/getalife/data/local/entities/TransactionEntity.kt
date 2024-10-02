package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import app.tinygiants.getalife.domain.model.TransactionDirection
import kotlinx.datetime.Instant

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val accountId: Long,
    val categoryId: Long?,
    val amount: Double,
    val transactionPartner: String,
    val transactionDirection: TransactionDirection,
    val description: String,
    val timestamp: Instant
)