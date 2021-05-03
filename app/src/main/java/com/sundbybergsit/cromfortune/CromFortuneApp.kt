@file:Suppress("unused")

package com.sundbybergsit.cromfortune

import android.app.Application
import androidx.work.*
import com.sundbybergsit.cromfortune.notifications.NotificationUtil
import com.sundbybergsit.cromfortune.settings.StockMuteSettingsRepository
import com.sundbybergsit.cromfortune.stocks.StockOrder
import com.sundbybergsit.cromfortune.stocks.StockOrderRepositoryImpl
import java.time.Instant
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class CromFortuneApp : Application(), Configuration.Provider {

    var lastRefreshed: Instant = Instant.ofEpochMilli(0L)

    override fun onCreate() {
        super.onCreate()
        upgradeDb()
        NotificationUtil.createChannel(applicationContext)
        StockMuteSettingsRepository.init(applicationContext)
        val workManager = WorkManager.getInstance(applicationContext)
        retrieveDataInBackground(workManager)
    }

    private fun upgradeDb() {
        // 0.2.7 -> 0.2.8
        val stockOrderRepositoryImpl = StockOrderRepositoryImpl(this)
        val oldStockOrders = stockOrderRepositoryImpl.list("BUBL.ST").sortedBy { stockOrder -> stockOrder.dateInMillis }
        val newStockOrders = mutableSetOf<StockOrder>()
        for (stockOrder in oldStockOrders) {
            newStockOrders.add(StockOrder(stockOrder.orderAction, stockOrder.currency,
                    stockOrder.dateInMillis, "GBK.ST", stockOrder.pricePerStock, stockOrder.commissionFee,
                    stockOrder.quantity))
        }
        if (stockOrderRepositoryImpl.list("GBK.ST").isEmpty()) {
            stockOrderRepositoryImpl.remove("GBK.ST")
        }
        if (newStockOrders.isNotEmpty()) {
            stockOrderRepositoryImpl.putAll("GBK.ST", newStockOrders)
        }
        stockOrderRepositoryImpl.remove("BUBL.ST")
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
