package app.tinygiants.getalife.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import app.tinygiants.getalife.data.local.dao.AccountDao
import app.tinygiants.getalife.data.local.dao.CategoryDao
import app.tinygiants.getalife.data.local.dao.CategoryMonthlyStatusDao
import app.tinygiants.getalife.data.local.dao.GroupDao
import app.tinygiants.getalife.data.local.dao.TransactionDao
import app.tinygiants.getalife.data.local.entities.AccountEntity
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
        CategoryMonthlyStatusEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GetALifeDatabase : RoomDatabase() {

    abstract fun groupDao(): GroupDao
    abstract fun categoryDao(): CategoryDao
    abstract fun accountsDao(): AccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryMonthlyStatusDao(): CategoryMonthlyStatusDao

    companion object {
        @Volatile
        private var INSTANCE: GetALifeDatabase? = null

        fun getDatabase(context: Context): GetALifeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GetALifeDatabase::class.java,
                    "budget_database"
                )
                    .fallbackToDestructiveMigration()
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
    fun fromInstant(value: Instant) = value.toEpochMilliseconds()

    @TypeConverter
    fun toInstant(value: Long) = Instant.fromEpochMilliseconds(value)

    // endregion
}