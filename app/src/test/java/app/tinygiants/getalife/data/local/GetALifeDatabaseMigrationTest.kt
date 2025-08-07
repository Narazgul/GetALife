package app.tinygiants.getalife.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class GetALifeDatabaseMigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        GetALifeDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate8To9() {
        var db = helper.createDatabase(TEST_DB, 8).apply {
            // db has schema version 8. Insert some test data using SQL queries.
            // Test data before migration
            execSQL("INSERT INTO accounts VALUES (1, 'Test Account', 1000.0, 11, 1, 0, 1234567890000, 1234567890000)")
            execSQL("INSERT INTO categories VALUES (1, 1, '🏠', 'Housing', 500.0, null, null, 1, 0, null, 1234567890000, 1234567890000)")

            close()
        }

        // Re-open the database with version 9 and provide MIGRATION_8_9 as the migration process.
        db = helper.runMigrationsAndValidate(TEST_DB, 9, true, GetALifeDatabase.MIGRATION_8_9)

        // MigrationTestHelper automatically verifies the schema changes,
        // but you can also validate that the data was preserved
        db.query("SELECT * FROM budgets").use { cursor ->
            assert(cursor.count == 1) // Should have the default budget created
        }

        db.query("SELECT * FROM accounts").use { cursor ->
            assert(cursor.count == 1) // Original account should still be there
            cursor.moveToFirst()
            val budgetIdColumnIndex = cursor.getColumnIndex("budgetId")
            assert(budgetIdColumnIndex >= 0) // budgetId column should exist
            val budgetId = cursor.getString(budgetIdColumnIndex)
            assert(budgetId == "local-default-budget") // Should be assigned to default budget
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate9To10() {
        var db = helper.createDatabase(TEST_DB, 9).apply {
            // db has schema version 9. Insert some test data.
            execSQL("INSERT INTO budgets VALUES ('test-budget', 'Test Budget', 'test-user', 1234567890000, null)")
            execSQL("INSERT INTO accounts VALUES (1, 'local-default-budget', 'Test Account', 1000.0, 11, 1, 0, 1234567890000, 1234567890000)")

            close()
        }

        // Re-open the database with version 10 and provide MIGRATION_9_10.
        db = helper.runMigrationsAndValidate(TEST_DB, 10, true, GetALifeDatabase.MIGRATION_9_10)

        // Verify sync columns were added
        db.query("SELECT * FROM budgets").use { cursor ->
            cursor.moveToFirst()
            val lastModifiedAtIndex = cursor.getColumnIndex("lastModifiedAt")
            val isSyncedIndex = cursor.getColumnIndex("isSynced")
            assert(lastModifiedAtIndex >= 0) // lastModifiedAt column should exist
            assert(isSyncedIndex >= 0) // isSynced column should exist
            assert(cursor.getInt(isSyncedIndex) == 0) // Should default to not synced
        }

        db.query("SELECT * FROM accounts").use { cursor ->
            cursor.moveToFirst()
            val isSyncedIndex = cursor.getColumnIndex("isSynced")
            assert(isSyncedIndex >= 0) // isSynced column should exist
            assert(cursor.getInt(isSyncedIndex) == 0) // Should default to not synced
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        // Create the earliest version of the db.
        helper.createDatabase(TEST_DB, 4).apply {
            close()
        }

        // Open latest version of the database. Room will validate the schema
        // once all migrations execute.
        GetALifeDatabase.getDatabase(
            InstrumentationRegistry.getInstrumentation().targetContext
        ).apply {
            openHelper.writableDatabase
            close()
        }
    }
}