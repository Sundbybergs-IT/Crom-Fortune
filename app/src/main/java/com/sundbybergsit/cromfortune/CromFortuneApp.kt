@file:Suppress("unused")

package com.sundbybergsit.cromfortune

import android.app.Application
import androidx.work.*
import com.sundbybergsit.cromfortune.ui.notifications.NotificationUtil
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CromFortuneApp : Application(), Configuration.Provider {

    override fun onCreate() {
        super.onCreate()
        NotificationUtil.createChannel(applicationContext)
        retrieveStockPricesInBackground()
    }

    private fun retrieveStockPricesInBackground() {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val stockRetrievalWorkRequest = PeriodicWorkRequestBuilder<StockRetrievalCoroutineWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints).build()
        WorkManager.getInstance(applicationContext).enqueue(stockRetrievalWorkRequest)
    }

    override fun getWorkManagerConfiguration(): Configuration =
            Configuration.Builder()
                    .setExecutor(Executors.newSingleThreadExecutor())
                    .setMinimumLoggingLevel(android.util.Log.INFO)
                    .setWorkerFactory(StockRetrievalWorkerFactory())
                    .build()

}
