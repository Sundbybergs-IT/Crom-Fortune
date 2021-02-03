package com.sundbybergsit.cromfortune

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters

class StockRetrievalWorkerFactory : WorkerFactory() {

    override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters,
    ): ListenableWorker {
        return StockRetrievalCoroutineWorker(context = appContext, workerParameters = workerParameters)
    }

}
