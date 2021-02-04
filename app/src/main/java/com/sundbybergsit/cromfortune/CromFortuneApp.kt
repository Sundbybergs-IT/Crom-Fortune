@file:Suppress("unused")

package com.sundbybergsit.cromfortune

import android.app.Application
import androidx.work.*
import com.sundbybergsit.cromfortune.ui.notifications.NotificationUtil
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CromFortuneApp : Application(), Configuration.Provider {

    val currencyRates : MutableMap<String, Double> = mutableMapOf()
    lateinit var currencyRateWorkRequestId : UUID

    override fun onCreate() {
        super.onCreate()
        NotificationUtil.createChannel(applicationContext)
        val workManager = WorkManager.getInstance(applicationContext)
        val workRequest = retrieveCurrencyRatesInBackground(workManager)
        currencyRateWorkRequestId = workRequest.id
        retrieveStockPricesInBackground(workManager)
    }

    private fun retrieveCurrencyRatesInBackground(workManager: WorkManager): OneTimeWorkRequest {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val currencyRateRetrievalWorkRequest = OneTimeWorkRequestBuilder<CurrencyRateRetrievalCoroutineWorker>()
                .setConstraints(constraints).build()
         workManager.enqueue(currencyRateRetrievalWorkRequest)
        return currencyRateRetrievalWorkRequest
    }

    private fun retrieveStockPricesInBackground(workManager: WorkManager) {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val stockRetrievalWorkRequest = PeriodicWorkRequestBuilder<StockRetrievalCoroutineWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints).build()
        workManager.enqueue(stockRetrievalWorkRequest)
    }

    override fun getWorkManagerConfiguration(): Configuration =
            Configuration.Builder()
                    .setExecutor(Executors.newSingleThreadExecutor())
                    .setMinimumLoggingLevel(android.util.Log.INFO)
                    .setWorkerFactory(StockRetrievalWorkerFactory())
                    .build()

}
