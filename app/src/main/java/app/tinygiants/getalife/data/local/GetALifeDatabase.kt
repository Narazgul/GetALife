package app.tinygiants.getalife.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
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
import app.tinygiants.getalife.domain.model.RepeatFrequency
import app.tinygiants.getalife.domain.model.TargetType
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
    version = 2,
    exportSchema = false
)
@TypeConverters(GetALifeDatabase.Converters::class)
abstract class GetALifeDatabase : RoomDatabase() {

    abstract val accountDao: AccountDao
    abstract val categoryDao: CategoryDao
    abstract val transactionDao: TransactionDao
    abstract val groupDao: GroupDao
    abstract val categoryMonthlyStatusDao: CategoryMonthlyStatusDao
    abstract val budgetDao: BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: GetALifeDatabase? = null

        fun getDatabase(context: Context): GetALifeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room
                    .databaseBuilder(
                        context.applicationContext,
                        GetALifeDatabase::class.java,
                        "budget_database"
                    )
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    // region Type Converters

    class Converters {

        @TypeConverter
        fun fromAccountType(value: AccountType) = when (value) {
            AccountType.Unknown -> 0
            AccountType.Cash -> 10
            AccountType.Checking -> 11
            AccountType.Savings -> 12
            AccountType.CreditCard -> 13
            AccountType.Depot -> 14
            AccountType.Loan -> 15
            AccountType.Mortgage -> 16
        }

        @TypeConverter
        fun toAccountType(value: Int) = when (value) {
            10 -> AccountType.Cash
            11 -> AccountType.Checking
            12 -> AccountType.Savings
            13 -> AccountType.CreditCard
            14 -> AccountType.Depot
            15 -> AccountType.Loan
            16 -> AccountType.Mortgage
            else -> AccountType.Unknown
        }

        @TypeConverter
        fun fromInstant(value: Instant): Long = value.toEpochMilliseconds()

        @TypeConverter
        fun toInstant(value: Long): Instant = Instant.fromEpochMilliseconds(value)

        // TargetType Converters
        @TypeConverter
        fun fromTargetType(value: TargetType) = when (value) {
            TargetType.NONE -> 0
            TargetType.NEEDED_FOR_SPENDING -> 1
            TargetType.SAVINGS_BALANCE -> 2
        }

        @TypeConverter
        fun toTargetType(value: Int) = when (value) {
            1 -> TargetType.NEEDED_FOR_SPENDING
            2 -> TargetType.SAVINGS_BALANCE
            else -> TargetType.NONE
        }

        // RepeatFrequency Converters
        @TypeConverter
        fun fromRepeatFrequency(value: RepeatFrequency) = when (value) {
            RepeatFrequency.NEVER -> 0
            RepeatFrequency.YEARLY -> 1
        }

        @TypeConverter
        fun toRepeatFrequency(value: Int) = when (value) {
            1 -> RepeatFrequency.YEARLY
            else -> RepeatFrequency.NEVER
        }
    }
}