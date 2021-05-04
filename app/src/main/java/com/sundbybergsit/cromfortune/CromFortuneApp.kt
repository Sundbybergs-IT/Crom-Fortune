@file:Suppress("unused")

package com.sundbybergsit.cromfortune

import android.app.Application
import androidx.work.*
import com.sundbybergsit.cromfortune.notifications.NotificationUtil
import com.sundbybergsit.cromfortune.settings.StockMuteSettingsRepository
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CromFortuneApp : Application(), Configuration.Provider {

    var lastRefreshed: Instant = Instant.ofEpochMilli(0L)

    override fun onCreate() {
        super.onCreate()
        NotificationUtil.createChannel(applicationContext)
        StockMuteSettingsRepository.init(applicationContext)
        val workManager = WorkManager.getInstance(applicationContext)
        retrieveDataInBackground(workManager)
    }

    private fun retrieveDataInBackground(workManager: WorkManager) {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val stockRetrievalWorkRequest = PeriodicWorkRequestBuilder<StockDataRetrievalCoroutineWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints).build()
        workManager.enqueueUniquePeriodicWork("fetchFromYahoo", ExistingPeriodicWorkPolicy.REPLACE,
                stockRetrievalWorkRequest)
    }

    override fun getWorkManagerConfiguration(): Configuration =
            Configuration.Builder()
                    .setExecutor(Executors.newSingleThreadExecutor())
                    .setMinimumLoggingLevel(android.util.Log.INFO)
                    .setWorkerFactory(StockRetrievalWorkerFactory())
                    .build()

}
