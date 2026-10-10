package com.sundbybergsit.cromfortune.main.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        PortfolioEntity::class,
        AssetTransactionEntity::class,
        StockOrderEntity::class,
        StockSplitEntity::class,
        AssetNoteEntity::class,
        StockMuteSettingsEntity::class,
        NotificationEntity::class
    ],
    version = 1,
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
                    .fallbackToDestructiveMigration()
                    .build()
            }
        }
    }
}
