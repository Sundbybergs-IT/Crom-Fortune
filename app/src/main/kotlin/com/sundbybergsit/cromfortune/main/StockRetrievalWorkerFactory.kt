package com.sundbybergsit.cromfortune.main

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters

class StockRetrievalWorkerFactory(
    private val marketDataClient: MarketDataClient = YahooMarketDataClient
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? {
        if (workerClassName != AssetDataRetrievalCoroutineWorker::class.java.name) return null
        return AssetDataRetrievalCoroutineWorker(
            context = appContext,
            workerParameters = workerParameters,
            marketDataClient = marketDataClient
        )
    }

}
