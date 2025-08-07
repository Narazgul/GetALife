package app.tinygiants.getalife.data.remote

import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.BudgetEntity
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.GroupEntity
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Instant

/**
 * Firestore data source that leverages Firebase's built-in offline capabilities.
 * Firestore automatically handles offline persistence, caching, and conflict resolution.
 */
@Singleton
class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    init {
        // Enable offline persistence (enabled by default on Android, but explicit is better)
        val settings = com.google.firebase.firestore.FirebaseFirestoreSettings.Builder()
            .build()
        firestore.firestoreSettings = settings
    }

    // Helper extension functions for conversion
    private fun BudgetEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "firebaseUserId" to firebaseUserId,
        "createdAt" to createdAt.toEpochMilliseconds(),
        "lastModifiedAt" to lastModifiedAt.toEpochMilliseconds()
    )

    private fun AccountEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "balance" to balance,
        "type" to type.name,
        "listPosition" to listPosition,
        "isClosed" to isClosed,
        "updatedAt" to updatedAt.toEpochMilliseconds(),
        "createdAt" to createdAt.toEpochMilliseconds(),
        "budgetId" to budgetId
    )

    private fun CategoryEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "groupId" to groupId,
        "emoji" to emoji,
        "name" to name,
        "budgetTarget" to budgetTarget,
        "monthlyTargetAmount" to monthlyTargetAmount,
        "targetMonthsRemaining" to targetMonthsRemaining,
        "listPosition" to listPosition,
        "isInitialCategory" to isInitialCategory,
        "linkedAccountId" to linkedAccountId,
        "updatedAt" to updatedAt.toEpochMilliseconds(),
        "createdAt" to createdAt.toEpochMilliseconds()
    )

    private fun GroupEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "listPosition" to listPosition,
        "isExpanded" to isExpanded
    )

    private fun TransactionEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
        "accountId" to accountId,
        "categoryId" to categoryId,
        "amount" to amount,
        "transactionPartner" to transactionPartner,
        "transactionDirection" to transactionDirection.name,
        "description" to description,
        "dateOfTransaction" to dateOfTransaction.toEpochMilliseconds(),
        "updatedAt" to updatedAt.toEpochMilliseconds(),
        "createdAt" to createdAt.toEpochMilliseconds(),
        "isRecurring" to isRecurring,
        "recurrenceFrequency" to recurrenceFrequency,
        "nextPaymentDate" to nextPaymentDate?.toEpochMilliseconds(),
        "recurrenceEndDate" to recurrenceEndDate?.toEpochMilliseconds(),
        "isRecurrenceActive" to isRecurrenceActive,
        "parentRecurringTransactionId" to parentRecurringTransactionId
    )

    private fun Map<String, Any?>.toBudgetEntity(id: String): BudgetEntity? {
        return try {
            BudgetEntity(
                id = id,
                name = this["name"] as String,
                firebaseUserId = this["firebaseUserId"] as String,
                createdAt = Instant.fromEpochMilliseconds(this["createdAt"] as Long),
                lastModifiedAt = Instant.fromEpochMilliseconds(this["lastModifiedAt"] as Long),
                isSynced = true, // Data from Firestore is considered synced
                lastSyncAt = Instant.fromEpochMilliseconds(System.currentTimeMillis())
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun Map<String, Any?>.toAccountEntity(id: Long): AccountEntity? {
        return try {
            AccountEntity(
                id = id,
                name = this["name"] as String,
                balance = (this["balance"] as Number).toDouble(),
                type = app.tinygiants.getalife.domain.model.AccountType.valueOf(this["type"] as String),
                listPosition = (this["listPosition"] as Number).toInt(),
                isClosed = this["isClosed"] as Boolean,
                updatedAt = Instant.fromEpochMilliseconds(this["updatedAt"] as Long),
                createdAt = Instant.fromEpochMilliseconds(this["createdAt"] as Long),
                budgetId = this["budgetId"] as String,
                isSynced = true
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun Map<String, Any?>.toCategoryEntity(id: Long): CategoryEntity? {
        return try {
            CategoryEntity(
                id = id,
                budgetId = this["budgetId"] as? String ?: "",
                groupId = (this["groupId"] as Number).toLong(),
                emoji = this["emoji"] as String,
                name = this["name"] as String,
                budgetTarget = (this["budgetTarget"] as Number).toDouble(),
                monthlyTargetAmount = (this["monthlyTargetAmount"] as? Number)?.toDouble(),
                targetMonthsRemaining = (this["targetMonthsRemaining"] as? Number)?.toInt(),
                listPosition = (this["listPosition"] as Number).toInt(),
                isInitialCategory = this["isInitialCategory"] as Boolean,
                linkedAccountId = (this["linkedAccountId"] as? Number)?.toLong(),
                updatedAt = Instant.fromEpochMilliseconds(this["updatedAt"] as Long),
                createdAt = Instant.fromEpochMilliseconds(this["createdAt"] as Long),
                isSynced = true
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun Map<String, Any?>.toGroupEntity(id: Long): GroupEntity? {
        return try {
            GroupEntity(
                id = id,
                budgetId = this["budgetId"] as? String ?: "",
                name = this["name"] as String,
                listPosition = (this["listPosition"] as Number).toInt(),
                isExpanded = this["isExpanded"] as Boolean,
                isSynced = true
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun Map<String, Any?>.toTransactionEntity(id: Long): TransactionEntity? {
        return try {
            TransactionEntity(
                id = id,
                budgetId = this["budgetId"] as? String ?: "",
                accountId = (this["accountId"] as Number).toLong(),
                categoryId = (this["categoryId"] as? Number)?.toLong(),
                amount = (this["amount"] as Number).toDouble(),
                transactionPartner = this["transactionPartner"] as String,
                transactionDirection = app.tinygiants.getalife.domain.model.TransactionDirection.valueOf(this["transactionDirection"] as String),
                description = this["description"] as String,
                dateOfTransaction = Instant.fromEpochMilliseconds(this["dateOfTransaction"] as Long),
                updatedAt = Instant.fromEpochMilliseconds(this["updatedAt"] as Long),
                createdAt = Instant.fromEpochMilliseconds(this["createdAt"] as Long),
                isRecurring = this["isRecurring"] as Boolean,
                recurrenceFrequency = this["recurrenceFrequency"] as? String,
                nextPaymentDate = (this["nextPaymentDate"] as? Long)?.let { Instant.fromEpochMilliseconds(it) },
                recurrenceEndDate = (this["recurrenceEndDate"] as? Long)?.let { Instant.fromEpochMilliseconds(it) },
                isRecurrenceActive = this["isRecurrenceActive"] as Boolean,
                parentRecurringTransactionId = (this["parentRecurringTransactionId"] as? Number)?.toLong(),
                isSynced = true
            )
        } catch (e: Exception) {
            null
        }
    }

    // Budget operations
    suspend fun saveBudget(budget: BudgetEntity) {
        firestore.collection("budgets")
            .document(budget.id)
            .set(budget.toFirestoreMap())
            .await()
    }

    suspend fun getBudgets(firebaseUserId: String): List<BudgetEntity> {
        return firestore.collection("budgets")
            .whereEqualTo("firebaseUserId", firebaseUserId)
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject<Map<String, Any?>>()?.toBudgetEntity(doc.id)
            }
    }

    suspend fun getBudget(budgetId: String): BudgetEntity? {
        return firestore.collection("budgets")
            .document(budgetId)
            .get()
            .await()
            .toObject<Map<String, Any?>>()?.toBudgetEntity(budgetId)
    }

    // Account operations
    suspend fun saveAccount(account: AccountEntity) {
        firestore.collection("budgets")
            .document(account.budgetId)
            .collection("accounts")
            .document(account.id.toString())
            .set(account.toFirestoreMap())
            .await()
    }

    suspend fun getAccounts(budgetId: String): List<AccountEntity> {
        return firestore.collection("budgets")
            .document(budgetId)
            .collection("accounts")
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject<Map<String, Any?>>()?.toAccountEntity(doc.id.toLongOrNull() ?: 0)
            }
    }

    // Category operations
    suspend fun saveCategory(category: CategoryEntity) {
        firestore.collection("budgets")
            .document(category.budgetId)
            .collection("categories")
            .document(category.id.toString())
            .set(category.toFirestoreMap())
            .await()
    }

    suspend fun getCategories(budgetId: String): List<CategoryEntity> {
        return firestore.collection("budgets")
            .document(budgetId)
            .collection("categories")
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject<Map<String, Any?>>()?.toCategoryEntity(doc.id.toLongOrNull() ?: 0)
            }
    }

    // Group operations
    suspend fun saveGroup(group: GroupEntity) {
        firestore.collection("budgets")
            .document(group.budgetId)
            .collection("groups")
            .document(group.id.toString())
            .set(group.toFirestoreMap())
            .await()
    }

    suspend fun getGroups(budgetId: String): List<GroupEntity> {
        return firestore.collection("budgets")
            .document(budgetId)
            .collection("groups")
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject<Map<String, Any?>>()?.toGroupEntity(doc.id.toLongOrNull() ?: 0)
            }
    }

    // Transaction operations
    suspend fun saveTransaction(transaction: TransactionEntity) {
        firestore.collection("budgets")
            .document(transaction.budgetId)
            .collection("transactions")
            .document(transaction.id.toString())
            .set(transaction.toFirestoreMap())
            .await()
    }

    suspend fun getTransactions(budgetId: String): List<TransactionEntity> {
        return firestore.collection("budgets")
            .document(budgetId)
            .collection("transactions")
            .get()
            .await()
            .documents
            .mapNotNull { doc ->
                doc.toObject<Map<String, Any?>>()?.toTransactionEntity(doc.id.toLongOrNull() ?: 0)
            }
    }
}