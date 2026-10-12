package com.sundbybergsit.cromfortune.main.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.main.CromTestRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class CromFortuneDatabaseMigrationTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @get:Rule
    val cromTestRule = CromTestRule()

    @Test
    fun `MIGRATION_1_2 and MIGRATION_2_3 - preserves all user data across migrations`() {
        val roomDb = Room.inMemoryDatabaseBuilder(context, CromFortuneDatabase::class.java)
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .allowMainThreadQueries()
            .build()

        val sqliteDb = roomDb.openHelper.writableDatabase

        // Step 1: Create v1 schema and seed user data manually
        sqliteDb.execSQL("CREATE TABLE IF NOT EXISTS `portfolios` (`name` TEXT NOT NULL, PRIMARY KEY(`name`))")
        sqliteDb.execSQL("INSERT INTO `portfolios` (`name`) VALUES ('Default')")
        sqliteDb.execSQL("INSERT INTO `portfolios` (`name`) VALUES ('Crom')")

        // Legacy v1 database only contained stock_orders
        sqliteDb.execSQL("CREATE TABLE IF NOT EXISTS `stock_orders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `portfolioName` TEXT NOT NULL, `assetId` TEXT NOT NULL, `orderAction` TEXT NOT NULL, `currency` TEXT NOT NULL, `dateInMillis` INTEGER NOT NULL, `name` TEXT NOT NULL, `pricePerStock` REAL NOT NULL, `commissionFee` REAL NOT NULL, `quantity` TEXT NOT NULL, `assetType` TEXT NOT NULL, `schemaVersion` INTEGER NOT NULL)")
        sqliteDb.execSQL("INSERT INTO `stock_orders` (`id`, `portfolioName`, `assetId`, `orderAction`, `currency`, `dateInMillis`, `name`, `pricePerStock`, `commissionFee`, `quantity`, `assetType`, `schemaVersion`) VALUES (1, 'Default', 'stock:INTC', 'Buy', 'USD', 1000, 'INTC', 20.0, 0.0, '5', 'STOCK', 1)")

        sqliteDb.execSQL("CREATE TABLE IF NOT EXISTS `asset_notes` (`assetId` TEXT NOT NULL, `text` TEXT NOT NULL, PRIMARY KEY(`assetId`))")
        sqliteDb.execSQL("INSERT INTO `asset_notes` (`assetId`, `text`) VALUES ('stock:INTC', 'Intel note')")

        sqliteDb.execSQL("CREATE TABLE IF NOT EXISTS `stock_mute_settings` (`stockSymbol` TEXT NOT NULL, `muted` INTEGER NOT NULL, PRIMARY KEY(`stockSymbol`))")
        sqliteDb.execSQL("INSERT INTO `stock_mute_settings` (`stockSymbol`, `muted`) VALUES ('INTC', 1)")

        sqliteDb.execSQL("CREATE TABLE IF NOT EXISTS `notifications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dateInMillis` INTEGER NOT NULL, `message` TEXT NOT NULL)")
        sqliteDb.execSQL("INSERT INTO `notifications` (`id`, `dateInMillis`, `message`) VALUES (1, 2000, 'Recommendation alert')")

        // Step 2: Execute MIGRATION_1_2 (creates asset_transactions and copies legacy stock_orders into asset_transactions)
        MIGRATION_1_2.migrate(sqliteDb)

        // Verify stock_orders table was present in v2
        var cursor = sqliteDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='stock_orders'")
        assertTrue("stock_orders table exists in v2 schema", cursor.moveToFirst())
        cursor.close()

        // Step 3: Execute MIGRATION_2_3 (dropping deprecated stock_orders table)
        MIGRATION_2_3.migrate(sqliteDb)

        // Verify stock_orders table was dropped
        cursor = sqliteDb.query("SELECT name FROM sqlite_master WHERE type='table' AND name='stock_orders'")
        assertFalse("stock_orders table dropped in v3 schema", cursor.moveToFirst())
        cursor.close()

        // Step 4: Verify all user data content across all DAOs in Room
        val portfolios = roomDb.portfolioDao().getAllPortfoliosList()
        assertEquals(listOf("Default", "Crom"), portfolios)

        val transactions = roomDb.assetTransactionDao().getTransactionsForPortfolio("Default")
        assertEquals(1, transactions.size)
        val transaction = transactions.first()
        assertEquals("stock:INTC", transaction.assetId)
        assertEquals("INTC", transaction.symbol)
        assertEquals("INTC", transaction.displayName)
        assertEquals("20.0", transaction.unitPrice)
        assertEquals("5", transaction.quantity)

        val note = roomDb.assetNoteDao().getNote("stock:INTC")
        assertNotNull(note)
        assertEquals("Intel note", note!!.text)

        val muteSettings = roomDb.stockMuteSettingsDao().getAllMuteSettings()
        assertEquals(1, muteSettings.size)
        assertEquals("INTC", muteSettings.first().stockSymbol)
        assertTrue(muteSettings.first().muted)

        val notifications = roomDb.notificationDao().getAllNotifications()
        assertEquals(1, notifications.size)
        val notification = notifications.first()
        assertEquals(2000L, notification.dateInMillis)
        assertEquals("Recommendation alert", notification.message)

        roomDb.close()
    }
}
