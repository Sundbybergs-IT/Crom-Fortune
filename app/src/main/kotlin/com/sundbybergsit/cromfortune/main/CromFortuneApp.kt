@file:Suppress("unused")

package com.sundbybergsit.cromfortune.main

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.work.*
import com.sundbybergsit.cromfortune.main.notifications.NotificationUtil
import com.sundbybergsit.cromfortune.main.settings.StockMuteSettingsRepository
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CromFortuneApp : Application(), Configuration.Provider {

    var lastRefreshed: Instant = Instant.ofEpochMilli(0L)

    override fun onCreate() {
        super.onCreate()
        System.setProperty("http.agent", "");
        NotificationUtil.createChannel(applicationContext)
        StockMuteSettingsRepository.init(applicationContext)
        val workManager = WorkManager.getInstance(applicationContext)
        createDataIfMissing(Databases.PORTFOLIO_DB_NAME)
        migrateOldData(fromDb = "Stocks", toDb = PortfolioRepository.DEFAULT_PORTFOLIO_NAME)
        migrateOldData(fromDb = "SPLITS", toDb = PortfolioRepository.DEFAULT_PORTFOLIO_NAME + "-splits")
        PortfolioRepository.init(
            getSharedPreferences(
                Databases.PORTFOLIO_DB_NAME,
                Context.MODE_PRIVATE
            )
        )
        retrieveDataInBackground(workManager)
    }

    private fun createDataIfMissing(db: String) {
        val sharedPreferences = getSharedPreferences(db, Context.MODE_PRIVATE)
        if (sharedPreferences.all.isEmpty()) {
            sharedPreferences.edit().putStringSet(
                Databases.PORTFOLIO_DB_KEY_NAME_STRING_SET,
                mutableSetOf(PortfolioRepository.DEFAULT_PORTFOLIO_NAME, PortfolioRepository.CROM_PORTFOLIO_NAME)
            ).apply()
        }
    }

    private fun migrateOldData(fromDb: String, toDb: String) {
        val oldPrefs = getSharedPreferences(fromDb, Context.MODE_PRIVATE)
        if (oldPrefs.all.isNotEmpty()) {
            Log.i("CromFortuneApp", "Migrating old data...")
            oldPrefs.copyTo(getSharedPreferences(toDb, Context.MODE_PRIVATE))
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

    private fun retrieveDataInBackground(workManager: WorkManager) {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val stockRetrievalWorkRequest = PeriodicWorkRequestBuilder<StockDataRetrievalCoroutineWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints).build()
        workManager.cancelAllWork()
        workManager.enqueueUniquePeriodicWork(
            "fetchFromYahoo", ExistingPeriodicWorkPolicy.UPDATE,
            stockRetrievalWorkRequest
        )
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setExecutor(Executors.newSingleThreadExecutor())
            .setMinimumLoggingLevel(Log.INFO)
            .setWorkerFactory(StockRetrievalWorkerFactory(PortfolioRepository))
            .build()

}
