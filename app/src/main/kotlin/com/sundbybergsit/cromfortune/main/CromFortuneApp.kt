package com.sundbybergsit.cromfortune.main

import android.app.Application
import android.util.Log
import androidx.work.Configuration
import com.sundbybergsit.cromfortune.main.notes.AssetNoteRepository
import com.sundbybergsit.cromfortune.main.notifications.NotificationUtil
import com.sundbybergsit.cromfortune.main.settings.StockMuteSettingsRepository
import com.sundbybergsit.cromfortune.main.settings.StockRetrievalSettings
import com.sundbybergsit.cromfortune.main.settings.ThemeSettingsRepository
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
        createDataIfMissing(Databases.PORTFOLIO_DB_NAME)
        PortfolioRepository.init(
            applicationContext
        )
        AssetRefreshScheduler.schedule(
            applicationContext,
            StockRetrievalSettings(applicationContext).timeInterval.value.refreshInterval
        )
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

}
