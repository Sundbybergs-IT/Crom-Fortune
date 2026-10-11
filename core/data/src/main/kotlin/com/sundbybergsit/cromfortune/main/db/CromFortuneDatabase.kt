package com.sundbybergsit.cromfortune.main.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `portfolios` (`name` TEXT NOT NULL, PRIMARY KEY(`name`))")

        db.execSQL("CREATE TABLE IF NOT EXISTS `asset_transactions` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `portfolioName` TEXT NOT NULL, `assetId` TEXT NOT NULL, `assetType` TEXT NOT NULL, `symbol` TEXT NOT NULL, `displayName` TEXT NOT NULL, `quoteCurrencyCode` TEXT NOT NULL, `action` TEXT NOT NULL, `dateInMillis` INTEGER NOT NULL, `unitPrice` TEXT NOT NULL, `commissionFee` TEXT NOT NULL, `quantity` TEXT NOT NULL, `schemaVersion` INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_asset_transactions_portfolioName` ON `asset_transactions` (`portfolioName`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_asset_transactions_assetId` ON `asset_transactions` (`assetId`)")

        db.execSQL("CREATE TABLE IF NOT EXISTS `stock_orders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `portfolioName` TEXT NOT NULL, `assetId` TEXT NOT NULL, `orderAction` TEXT NOT NULL, `currency` TEXT NOT NULL, `dateInMillis` INTEGER NOT NULL, `name` TEXT NOT NULL, `pricePerStock` REAL NOT NULL, `commissionFee` REAL NOT NULL, `quantity` TEXT NOT NULL, `assetType` TEXT NOT NULL, `schemaVersion` INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_orders_portfolioName` ON `stock_orders` (`portfolioName`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_orders_assetId` ON `stock_orders` (`assetId`)")

        db.execSQL("CREATE TABLE IF NOT EXISTS `stock_splits` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `portfolioName` TEXT NOT NULL, `name` TEXT NOT NULL, `dateInMillis` INTEGER NOT NULL, `quantity` INTEGER NOT NULL, `reverse` INTEGER NOT NULL)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_splits_portfolioName` ON `stock_splits` (`portfolioName`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_stock_splits_name` ON `stock_splits` (`name`)")

        db.execSQL("CREATE TABLE IF NOT EXISTS `asset_notes` (`assetId` TEXT NOT NULL, `text` TEXT NOT NULL, PRIMARY KEY(`assetId`))")

        db.execSQL("CREATE TABLE IF NOT EXISTS `stock_mute_settings` (`stockSymbol` TEXT NOT NULL, `muted` INTEGER NOT NULL, PRIMARY KEY(`stockSymbol`))")

        db.execSQL("CREATE TABLE IF NOT EXISTS `notifications` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `dateInMillis` INTEGER NOT NULL, `message` TEXT NOT NULL, `portfolioName` TEXT, `stockSymbol` TEXT, `currencyCode` TEXT, `pricePerStock` REAL, `orderAction` TEXT)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_notifications_portfolioName` ON `notifications` (`portfolioName`)")

        db.execSQL("CREATE TABLE IF NOT EXISTS `asset_prices` (`assetId` TEXT NOT NULL, `currencyCode` TEXT NOT NULL, `price` REAL NOT NULL, `lastUpdatedAt` INTEGER NOT NULL, `isStale` INTEGER NOT NULL, PRIMARY KEY(`assetId`))")

        addColumnIfNotExists(db, "notifications", "portfolioName", "TEXT")
        addColumnIfNotExists(db, "notifications", "stockSymbol", "TEXT")
        addColumnIfNotExists(db, "notifications", "currencyCode", "TEXT")
        addColumnIfNotExists(db, "notifications", "pricePerStock", "REAL")
        addColumnIfNotExists(db, "notifications", "orderAction", "TEXT")
    }

    private fun addColumnIfNotExists(db: SupportSQLiteDatabase, table: String, column: String, typeAndConstraints: String) {
        val cursor = db.query("PRAGMA table_info(`$table`)")
        var exists = false
        while (cursor.moveToNext()) {
            val nameIndex = cursor.getColumnIndex("name")
            if (nameIndex != -1 && cursor.getString(nameIndex) == column) {
                exists = true
                break
            }
        }
        cursor.close()
        if (!exists) {
            db.execSQL("ALTER TABLE `$table` ADD COLUMN `$column` $typeAndConstraints")
        }
    }
}

@Database(
    entities = [
        PortfolioEntity::class,
        AssetTransactionEntity::class,
        StockOrderEntity::class,
        StockSplitEntity::class,
        AssetNoteEntity::class,
        StockMuteSettingsEntity::class,
        NotificationEntity::class,
        AssetPriceEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class CromFortuneDatabase : RoomDatabase() {

    abstract fun portfolioDao(): PortfolioDao
    abstract fun assetTransactionDao(): AssetTransactionDao
    abstract fun stockOrderDao(): StockOrderDao
    abstract fun stockSplitDao(): StockSplitDao
    abstract fun assetNoteDao(): AssetNoteDao
    abstract fun stockMuteSettingsDao(): StockMuteSettingsDao
    abstract fun notificationDao(): NotificationDao
    abstract fun assetPriceDao(): AssetPriceDao

    companion object {
        @Volatile
        private var INSTANCE: CromFortuneDatabase? = null

        fun getInstance(context: Context): CromFortuneDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = createDatabase(context)
                INSTANCE = instance
                instance
            }
        }

        fun setInstance(database: CromFortuneDatabase?) {
            INSTANCE = database
        }

        private fun createDatabase(context: Context): CromFortuneDatabase {
            val isTest = runCatching { Class.forName("org.robolectric.Robolectric") }.isSuccess
            return if (isTest) {
                Room.inMemoryDatabaseBuilder(
                    context.applicationContext,
                    CromFortuneDatabase::class.java
                )
                    .allowMainThreadQueries()
                    .build()
            } else {
                Room.databaseBuilder(
                    context.applicationContext,
                    CromFortuneDatabase::class.java,
                    "crom_fortune_database"
                )
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_1_2)
                    .build()
            }
        }
    }
}
