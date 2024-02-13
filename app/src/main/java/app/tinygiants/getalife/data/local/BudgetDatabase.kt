package app.tinygiants.getalife.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import app.tinygiants.getalife.data.local.dao.BudgetDao
import app.tinygiants.getalife.data.local.dao.CategoryDao
import app.tinygiants.getalife.data.local.dao.HeaderDao
import app.tinygiants.getalife.data.local.entities.BudgetEntity
import app.tinygiants.getalife.data.local.entities.CategoryEntity
import app.tinygiants.getalife.data.local.entities.HeaderEntity

@Database(
    entities = [BudgetEntity::class, HeaderEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun budgetDao(): BudgetDao
    abstract fun headerDao(): HeaderDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "budget_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromLongList(value: List<Long>?): String {
        return value?.joinToString(",") ?: ""
    }

    @TypeConverter
    fun toLongList(value: String): List<Long> {
        return if (value.isNotEmpty()) value.split(",").map { it.toLong() } else emptyList()
    }
}