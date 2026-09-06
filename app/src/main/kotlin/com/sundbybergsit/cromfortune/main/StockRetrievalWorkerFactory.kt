package com.sundbybergsit.cromfortune.main

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters

class StockRetrievalWorkerFactory(
    private val stockMarketDataClient: StockMarketDataClient = YahooStockMarketDataClient
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker {
        return StockDataRetrievalCoroutineWorker(
            context = appContext,
            workerParameters = workerParameters,
            stockMarketDataClient = stockMarketDataClient
        )
    }

}
