package com.sundbybergsit.cromfortune.main

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import androidx.work.Configuration
import com.sundbybergsit.cromfortune.main.notes.AssetNoteRepository
import com.sundbybergsit.cromfortune.main.notifications.NotificationUtil
import com.sundbybergsit.cromfortune.main.settings.StockMuteSettingsRepository
import com.sundbybergsit.cromfortune.main.settings.StockRetrievalSettings
import com.sundbybergsit.cromfortune.main.settings.ThemeSettingsRepository
import com.sundbybergsit.cromfortune.main.stocks.StockOrderPersistenceMigration
import java.net.CookieHandler
import java.net.CookieManager

class CromFortuneApp : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        System.setProperty("yahoofinance.scrapeurl.histquotes2", "https://fc.yahoo.com")
        System.setProperty("http.agent", "")
        CookieHandler.setDefault(CookieManager())
        NotificationUtil.createChannel(applicationContext)
        StockMuteSettingsRepository.init(applicationContext)
        AssetNoteRepository.init(applicationContext)
        ThemeSettingsRepository.init(applicationContext)
        AssetRefreshStatusRepository.init(applicationContext)
        migrateOldData(fromDb = "Stocks", toDb = PortfolioRepository.DEFAULT_PORTFOLIO_NAME)
        migrateOldData(fromDb = "SPLITS", toDb = PortfolioRepository.DEFAULT_PORTFOLIO_NAME + "-splits")
        createDataIfMissing(Databases.PORTFOLIO_DB_NAME)
        PortfolioRepository.init(
            applicationContext
        )
        StockOrderPersistenceMigration.migrateToLatest(
            context = applicationContext,
            portfolioNames = PortfolioRepository.portfolioNamesStateFlow.value
        )
        runCatching {
            AssetRefreshScheduler.schedule(
                applicationContext,
                StockRetrievalSettings(applicationContext).timeInterval.value.refreshInterval
            )
        }
    }

    private fun createDataIfMissing(db: String) {
        val sharedPreferences = getSharedPreferences(db, MODE_PRIVATE)
        if (sharedPreferences.all.isEmpty()) {
            sharedPreferences.edit().putStringSet(
                Databases.PORTFOLIO_DB_KEY_NAME_STRING_SET,
                mutableSetOf(PortfolioRepository.DEFAULT_PORTFOLIO_NAME, PortfolioRepository.CROM_PORTFOLIO_NAME)
            ).apply()
        }
    }

    private fun migrateOldData(fromDb: String, toDb: String) {
        val oldPrefs = getSharedPreferences(fromDb, MODE_PRIVATE)
        if (oldPrefs.all.isNotEmpty()) {
            Log.i("CromFortuneApp", "Migrating old data...")
            oldPrefs.copyTo(getSharedPreferences(toDb, MODE_PRIVATE))
            oldPrefs.edit().clear().apply()
            Log.i("CromFortuneApp", "Done migrating.")
        }
    }

    private fun SharedPreferences.copyTo(dest: SharedPreferences) = with(dest.edit()) {
        for (entry in all.entries) {
            val value = entry.value ?: continue
            val key = entry.key
            when (value) {
                is String -> putString(key, value)
                is Set<*> -> putStringSet(key, value as Set<String>)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is Float -> putFloat(key, value)
                is Boolean -> putBoolean(key, value)
                else -> error("Unknown value type: $value")
            }
        }
        apply()
    }

}
