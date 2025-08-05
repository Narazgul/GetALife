package app.tinygiants.getalife.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.tinygiants.getalife.data.local.GetALifeDatabase.Companion.MIGRATION_5_6
import app.tinygiants.getalife.data.local.GetALifeDatabase.Companion.MIGRATION_6_7
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        GetALifeDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate5To6() {
        // Create database with version 5
        var db = helper.createDatabase(TEST_DB, 5).apply {
            // Insert test data in version 5 format (without recurring payment columns)
            execSQL(
                """
                INSERT INTO transactions (
                    id, accountId, categoryId, amount, transactionPartner, 
                    transactionDirection, description, dateOfTransaction, updatedAt, createdAt
                ) VALUES (
                    1, 1, 1, -1500.0, 'Landlord', 'Outflow', 'Rent', 
                    1704067200000, 1704067200000, 1704067200000
                )
            """.trimIndent()
            )
            close()
        }

        // Run the migration to version 6
        db = helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6)

        // Verify new columns exist with correct default values
        val cursor = db.query("SELECT * FROM transactions WHERE id = 1")
        assertTrue(cursor.moveToFirst())

        // Check that new columns exist and have correct default values
        val isRecurringIndex = cursor.getColumnIndex("isRecurring")
        val recurrenceFrequencyIndex = cursor.getColumnIndex("recurrenceFrequency")
        val nextPaymentDateIndex = cursor.getColumnIndex("nextPaymentDate")
        val recurrenceEndDateIndex = cursor.getColumnIndex("recurrenceEndDate")
        val isRecurrenceActiveIndex = cursor.getColumnIndex("isRecurrenceActive")
        val parentRecurringTransactionIdIndex = cursor.getColumnIndex("parentRecurringTransactionId")

        assertTrue(isRecurringIndex >= 0, "isRecurring column should exist")
        assertTrue(recurrenceFrequencyIndex >= 0, "recurrenceFrequency column should exist")
        assertTrue(nextPaymentDateIndex >= 0, "nextPaymentDate column should exist")
        assertTrue(recurrenceEndDateIndex >= 0, "recurrenceEndDate column should exist")
        assertTrue(isRecurrenceActiveIndex >= 0, "isRecurrenceActive column should exist")
        assertTrue(parentRecurringTransactionIdIndex >= 0, "parentRecurringTransactionId column should exist")

        // Verify default values
        assertEquals(0, cursor.getInt(isRecurringIndex), "isRecurring should default to 0")
        assertTrue(cursor.isNull(recurrenceFrequencyIndex), "recurrenceFrequency should default to null")
        assertTrue(cursor.isNull(nextPaymentDateIndex), "nextPaymentDate should default to null")
        assertTrue(cursor.isNull(recurrenceEndDateIndex), "recurrenceEndDate should default to null")
        assertEquals(1, cursor.getInt(isRecurrenceActiveIndex), "isRecurrenceActive should default to 1")
        assertTrue(cursor.isNull(parentRecurringTransactionIdIndex), "parentRecurringTransactionId should default to null")

        cursor.close()
        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrate6To7() {
        // Create database with version 6 (with recurring payment columns)
        var db = helper.createDatabase(TEST_DB, 6).apply {
            // Insert test data including a recurring transaction with null frequency
            execSQL(
                """
                INSERT INTO transactions (
                    id, accountId, categoryId, amount, transactionPartner, 
                    transactionDirection, description, dateOfTransaction, updatedAt, createdAt,
                    isRecurring, recurrenceFrequency, nextPaymentDate, recurrenceEndDate, 
                    isRecurrenceActive, parentRecurringTransactionId
                ) VALUES (
                    1, 1, 1, -1500.0, 'Landlord', 'Outflow', 'Rent', 
                    1704067200000, 1704067200000, 1704067200000,
                    1, NULL, 1704153600000, NULL, 1, NULL
                )
            """.trimIndent()
            )

            execSQL(
                """
                INSERT INTO transactions (
                    id, accountId, categoryId, amount, transactionPartner, 
                    transactionDirection, description, dateOfTransaction, updatedAt, createdAt,
                    isRecurring, recurrenceFrequency, nextPaymentDate, recurrenceEndDate, 
                    isRecurrenceActive, parentRecurringTransactionId
                ) VALUES (
                    2, 1, 1, -800.0, 'Utility Company', 'Outflow', 'Electricity', 
                    1704067200000, 1704067200000, 1704067200000,
                    0, NULL, NULL, NULL, 1, NULL
                )
            """.trimIndent()
            )
            close()
        }

        // Run the migration to version 7
        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7)

        // Verify indices were created
        val indexCursor = db.query("SELECT name FROM sqlite_master WHERE type = 'index' AND tbl_name = 'transactions'")
        val indexNames = mutableSetOf<String>()
        while (indexCursor.moveToNext()) {
            indexNames.add(indexCursor.getString(0))
        }
        indexCursor.close()

        assertTrue(indexNames.contains("index_transactions_isRecurring"), "Should have isRecurring index")
        assertTrue(indexNames.contains("index_transactions_nextPaymentDate"), "Should have nextPaymentDate index")
        assertTrue(indexNames.contains("idx_recurring_payments"), "Should have composite recurring payments index")

        // Verify that null frequency was updated to MONTHLY for recurring transactions
        val dataCursor = db.query("SELECT * FROM transactions WHERE id = 1")
        assertTrue(dataCursor.moveToFirst())

        val recurrenceFrequencyIndex = dataCursor.getColumnIndex("recurrenceFrequency")
        assertEquals(
            "MONTHLY", dataCursor.getString(recurrenceFrequencyIndex),
            "Null frequency should be updated to MONTHLY for recurring transactions"
        )

        dataCursor.close()

        // Verify non-recurring transaction was not affected
        val nonRecurringCursor = db.query("SELECT * FROM transactions WHERE id = 2")
        assertTrue(nonRecurringCursor.moveToNext())
        val nonRecurringFrequencyIndex = nonRecurringCursor.getColumnIndex("recurrenceFrequency")
        assertTrue(
            nonRecurringCursor.isNull(nonRecurringFrequencyIndex),
            "Non-recurring transactions should keep null frequency"
        )
        nonRecurringCursor.close()

        db.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrateAll5To7() {
        // Test complete migration path 5 → 6 → 7
        var db = helper.createDatabase(TEST_DB, 5).apply {
            // Insert test data in version 5 format
            execSQL(
                """
                INSERT INTO transactions (
                    id, accountId, categoryId, amount, transactionPartner, 
                    transactionDirection, description, dateOfTransaction, updatedAt, createdAt
                ) VALUES (
                    1, 1, 1, -1500.0, 'Landlord', 'Outflow', 'Rent', 
                    1704067200000, 1704067200000, 1704067200000
                )
            """.trimIndent()
            )
            close()
        }

        // Run all migrations to version 7
        db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_5_6, MIGRATION_6_7)

        // Verify final state has all columns and indices
        val cursor = db.query("SELECT * FROM transactions WHERE id = 1")
        assertTrue(cursor.moveToFirst())

        // Verify all recurring payment columns exist
        assertTrue(cursor.getColumnIndex("isRecurring") >= 0)
        assertTrue(cursor.getColumnIndex("recurrenceFrequency") >= 0)
        assertTrue(cursor.getColumnIndex("nextPaymentDate") >= 0)
        assertTrue(cursor.getColumnIndex("recurrenceEndDate") >= 0)
        assertTrue(cursor.getColumnIndex("isRecurrenceActive") >= 0)
        assertTrue(cursor.getColumnIndex("parentRecurringTransactionId") >= 0)

        cursor.close()

        // Verify indices exist
        val indexCursor = db.query("SELECT name FROM sqlite_master WHERE type = 'index' AND tbl_name = 'transactions'")
        val indexNames = mutableSetOf<String>()
        while (indexCursor.moveToNext()) {
            indexNames.add(indexCursor.getString(0))
        }
        indexCursor.close()

        assertTrue(indexNames.contains("index_transactions_isRecurring"))
        assertTrue(indexNames.contains("index_transactions_nextPaymentDate"))
        assertTrue(indexNames.contains("idx_recurring_payments"))

        db.close()
    }
}