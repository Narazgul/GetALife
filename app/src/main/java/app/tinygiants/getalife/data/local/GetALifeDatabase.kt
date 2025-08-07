package app.tinygiants.getalife.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import app.tinygiants.getalife.data.local.dao.AccountDao
import app.tinygiants.getalife.data.local.dao.BudgetDao
import app.tinygiants.getalife.data.local.dao.CategoryDao
import app.tinygiants.getalife.data.local.dao.CategoryMonthlyStatusDao
import app.tinygiants.getalife.data.local.dao.GroupDao
import app.tinygiants.getalife.data.local.dao.TransactionDao
import app.tinygiants.getalife.data.local.entities.AccountEntity
import app.tinygiants.getalife.data.local.entities.BudgetEntity
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.CategoryMonthlyStatusEntity
import app.tinygiants.getalife.data.local.entities.GroupEntity
import app.tinygiants.getalife.data.local.entities.TransactionEntity
import app.tinygiants.getalife.domain.model.AccountType
import kotlin.time.Instant

@Database(
    entities = [
        GroupEntity::class,
        CategoryEntity::class,
        AccountEntity::class,
        TransactionEntity::class,
        CategoryMonthlyStatusEntity::class,
        BudgetEntity::class
    ],
    version = 10,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GetALifeDatabase : RoomDatabase() {

    abstract fun groupDao(): GroupDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountsDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryMonthlyStatusDao(): CategoryMonthlyStatusDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: GetALifeDatabase? = null

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add linkedAccountId column to categories table
                db.execSQL("ALTER TABLE categories ADD COLUMN linkedAccountId INTEGER")

                // Note: behaviorType column removal not needed as it didn't exist in version 4
                // The column only existed in our code changes, not in the actual database
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add recurring payment columns to transactions table with default values
                db.execSQL("ALTER TABLE transactions ADD COLUMN isRecurring INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE transactions ADD COLUMN recurrenceFrequency TEXT")
                db.execSQL("ALTER TABLE transactions ADD COLUMN nextPaymentDate INTEGER")
                db.execSQL("ALTER TABLE transactions ADD COLUMN recurrenceEndDate INTEGER")
                db.execSQL("ALTER TABLE transactions ADD COLUMN isRecurrenceActive INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE transactions ADD COLUMN parentRecurringTransactionId INTEGER")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add indices for better recurring payment query performance
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_isRecurring` ON `transactions` (`isRecurring`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_nextPaymentDate` ON `transactions` (`nextPaymentDate`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `idx_recurring_payments` ON `transactions` (`isRecurring`, `nextPaymentDate`, `isRecurrenceActive`)")

                // Add constraint: recurrenceFrequency must not be null when isRecurring = 1
                // Note: SQLite doesn't support adding constraints to existing tables directly
                // We'll handle this validation in the application layer instead

                // Update any existing recurring transactions that might have null frequency to MONTHLY as default
                db.execSQL("UPDATE transactions SET recurrenceFrequency = 'MONTHLY' WHERE isRecurring = 1 AND recurrenceFrequency IS NULL")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add isClosed column to accounts table
                db.execSQL("ALTER TABLE accounts ADD COLUMN isClosed INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 1. Create new budgets table
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS budgets (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        firebaseUserId TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        lastSyncAt INTEGER
                    )
                    """.trimIndent()
                )

                // 2. Create a default local budget and remember its id for later updates
                val localBudgetId = "local-default-budget"
                val now = System.currentTimeMillis()
                db.execSQL(
                    "INSERT OR IGNORE INTO budgets (id, name, firebaseUserId, createdAt) VALUES ('$localBudgetId', 'Lokales Budget', '', $now)"
                )

                // 3. Add budgetId columns to existing tables where missing
                val tables = listOf("accounts", "categories", "groups", "transactions", "categoryMonthlyStatus")
                tables.forEach { table ->
                    db.execSQL("ALTER TABLE $table ADD COLUMN budgetId TEXT")
                    db.execSQL("UPDATE $table SET budgetId = '$localBudgetId' WHERE budgetId IS NULL")
                }
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add sync tracking fields for offline-first architecture

                // Update budgets table
                db.execSQL("ALTER TABLE budgets ADD COLUMN lastModifiedAt INTEGER")
                db.execSQL("ALTER TABLE budgets ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
                val now = System.currentTimeMillis()
                db.execSQL("UPDATE budgets SET lastModifiedAt = $now WHERE lastModifiedAt IS NULL")

                // Add isSynced columns to all data tables
                val dataTables = listOf("accounts", "categories", "groups", "transactions", "categoryMonthlyStatus")
                dataTables.forEach { table ->
                    db.execSQL("ALTER TABLE $table ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 0")
                }
                
                // Add indices for budgetId columns for better query performance
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_accounts_budgetId ON accounts(budgetId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_categories_budgetId ON categories(budgetId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_groups_budgetId ON `groups`(budgetId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_budgetId ON transactions(budgetId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_categoryMonthlyStatus_budgetId ON categoryMonthlyStatus(budgetId)")
                
                // Add indices for sync queries
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_accounts_sync ON accounts(isSynced)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_categories_sync ON categories(isSynced)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_groups_sync ON `groups`(isSynced)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_transactions_sync ON transactions(isSynced)")
                db.execSQL("CREATE INDEX IF NOT EXISTS idx_categoryMonthlyStatus_sync ON categoryMonthlyStatus(isSynced)")
            }
        }

        fun getDatabase(context: Context): GetALifeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GetALifeDatabase::class.java,
                    "budget_database"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

// region TypConverters

private const val UNKNOWN = 0
private const val CASH = 10
private const val CHECKING = 11
private const val SAVINGS = 12
private const val CREDIT_CARD = 13
private const val DEPOT = 14
private const val LOAN = 15
private const val MORTGAGE = 16

class Converters {

    @TypeConverter
    fun fromAccountType(value: AccountType) = when (value) {
        AccountType.Unknown -> UNKNOWN
        AccountType.Cash -> CASH
        AccountType.Checking -> CHECKING
        AccountType.Savings -> SAVINGS
        AccountType.CreditCard -> CREDIT_CARD
        AccountType.Depot -> DEPOT
        AccountType.Loan -> LOAN
        AccountType.Mortgage -> MORTGAGE
    }

    @TypeConverter
    fun toAccountType(value: Int) = when (value) {
        CASH -> AccountType.Cash
        CHECKING -> AccountType.Checking
        SAVINGS -> AccountType.Savings
        CREDIT_CARD -> AccountType.CreditCard
        DEPOT -> AccountType.Depot
        LOAN -> AccountType.Loan
        MORTGAGE -> AccountType.Mortgage
        else -> AccountType.Unknown
    }

    @TypeConverter
    fun fromInstant(value: Instant): Long = value.toEpochMilliseconds()

    @TypeConverter
    fun toInstant(value: Long): Instant = Instant.fromEpochMilliseconds(value)

    // endregion
}