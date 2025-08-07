package app.tinygiants.getalife.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant

/**
 * Room representation of a budget.
 * A budget groups together all accounts, categories and transactions that belong to a user context.
 * The `id` is stored as a String so that we can reuse the Firestore document id once the data is
 * synchronised with the backend. While the user is offline we create a random UUID which will later
 * be replaced by (or merged with) the Firestore id during the account-linking flow.
 */
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey
    val id: String, // UUID or Firestore document id
    val name: String,
    val firebaseUserId: String, // id of the authenticated Firebase user that owns this budget
    val createdAt: Instant,
    val lastSyncAt: Instant? = null, // timestamp of the last successful sync with Firestore
    val lastModifiedAt: Instant, // timestamp of the last local modification
    val isSynced: Boolean = false // tracks if this budget has been synced to Firestore
)
