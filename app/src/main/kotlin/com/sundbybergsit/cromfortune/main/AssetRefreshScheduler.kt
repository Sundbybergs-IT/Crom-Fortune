package com.sundbybergsit.cromfortune.main

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.sundbybergsit.cromfortune.main.settings.StockRetrievalSettings
import java.util.concurrent.TimeUnit

object AssetRefreshScheduler {
    private const val UNIQUE_WORK_NAME = "fetchFromYahoo"

    fun schedule(context: Context, interval: StockRetrievalSettings.RefreshInterval) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<AssetDataRetrievalCoroutineWorker>(
            interval.minutes.toLong(),
            TimeUnit.MINUTES
        ).setConstraints(constraints).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }
}
