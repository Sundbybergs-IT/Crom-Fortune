package com.sundbybergsit.cromfortune.main.db

import android.content.Context
import android.util.Log
import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockSplit
import com.sundbybergsit.cromfortune.domain.notifications.NotificationMessage
import com.sundbybergsit.cromfortune.main.Databases
import com.sundbybergsit.cromfortune.main.PortfolioRepository
import com.sundbybergsit.cromfortune.main.notes.AssetNote
import kotlinx.serialization.json.Json

object LegacySharedPreferencesMigrator {

    private const val TAG = "LegacyMigrator"

    fun migrate(context: Context) {
        val db = CromFortuneDatabase.getInstance(context)

        // 1. Migrate Portfolios
        val portfolioPrefs = context.getSharedPreferences(Databases.PORTFOLIO_DB_NAME, Context.MODE_PRIVATE)
        val legacyPortfolioNames = portfolioPrefs.getStringSet(Databases.PORTFOLIO_DB_KEY_NAME_STRING_SET, null)
        if (!legacyPortfolioNames.isNullOrEmpty()) {
            Log.i(TAG, "Migrating legacy portfolio names: $legacyPortfolioNames")
            val portfolioDao = db.portfolioDao()
            legacyPortfolioNames.forEach { portfolioDao.insert(PortfolioEntity(it)) }
            portfolioPrefs.edit().remove(Databases.PORTFOLIO_DB_KEY_NAME_STRING_SET).apply()
        }

        // Get all portfolios
        val portfolios = db.portfolioDao().getAllPortfoliosList().ifEmpty {
            listOf(PortfolioRepository.DEFAULT_PORTFOLIO_NAME, PortfolioRepository.CROM_PORTFOLIO_NAME)
        }

        // 2. Migrate Transactions, Orders, Splits for each portfolio
        for (portfolio in portfolios) {
            val portfolioPrefs = context.getSharedPreferences(portfolio, Context.MODE_PRIVATE)
            if (portfolioPrefs.all.isNotEmpty()) {
                Log.i(TAG, "Migrating transactions and orders for portfolio $portfolio")
                val transactionDao = db.assetTransactionDao()
                val orderDao = db.stockOrderDao()
                for ((_, value) in portfolioPrefs.all) {
                    val rawJson = value as? String
                        ?: (value as? Set<*>)?.firstOrNull() as? String
                        ?: continue
                    runCatching {
                        val transactions = Json.decodeFromString<Set<AssetTransaction>>(rawJson)
                        transactionDao.insertAll(transactions.map { AssetTransactionEntity.fromDomain(portfolio, it) })
                    }
                    runCatching {
                        val orders = Json.decodeFromString<Set<StockOrder>>(rawJson)
                        orderDao.insertAll(orders.map { StockOrderEntity.fromDomain(portfolio, it) })
                    }
                }
                portfolioPrefs.edit().clear().apply()
            }

            // Splits
            val splitPrefs = context.getSharedPreferences("$portfolio-splits", Context.MODE_PRIVATE)
            if (splitPrefs.all.isNotEmpty()) {
                Log.i(TAG, "Migrating stock splits for portfolio $portfolio")
                val splitDao = db.stockSplitDao()
                for ((_, value) in splitPrefs.all) {
                    val rawJson = value as? String ?: continue
                    runCatching {
                        val splits = Json.decodeFromString<Set<StockSplit>>(rawJson)
                        splitDao.insertAll(splits.map { StockSplitEntity.fromDomain(portfolio, it) })
                    }
                }
                splitPrefs.edit().clear().apply()
            }
        }

        // 3. Migrate Asset Notes
        val notePrefs = context.getSharedPreferences("AssetNotes", Context.MODE_PRIVATE)
        if (notePrefs.all.isNotEmpty()) {
            Log.i(TAG, "Migrating asset notes")
            val noteDao = db.assetNoteDao()
            for ((key, value) in notePrefs.all) {
                val rawJson = value as? String ?: continue
                runCatching {
                    val note = Json.decodeFromString<AssetNote>(rawJson)
                    noteDao.insert(AssetNoteEntity(key, note.text))
                }
            }
            notePrefs.edit().clear().apply()
        }

        // 4. Migrate Stock Mute Settings
        val mutePrefs = context.getSharedPreferences("StockMuteSettings", Context.MODE_PRIVATE)
        if (mutePrefs.all.isNotEmpty()) {
            Log.i(TAG, "Migrating stock mute settings")
            val muteDao = db.stockMuteSettingsDao()
            for ((key, value) in mutePrefs.all) {
                val muted = (value as? String)?.toBoolean() ?: false
                muteDao.insert(StockMuteSettingsEntity(key, muted))
            }
            mutePrefs.edit().clear().apply()
        }

        // 5. Migrate Notifications
        val notifPrefs = context.getSharedPreferences("Notifications", Context.MODE_PRIVATE)
        if (notifPrefs.all.isNotEmpty()) {
            Log.i(TAG, "Migrating notifications")
            val notifDao = db.notificationDao()
            for ((_, value) in notifPrefs.all) {
                val rawJson = value as? String ?: continue
                runCatching {
                    val message = Json.decodeFromString<NotificationMessage>(rawJson)
                    notifDao.insert(NotificationEntity.fromDomain(message))
                }
            }
            notifPrefs.edit().clear().apply()
        }
    }
}
