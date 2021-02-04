package com.sundbybergsit.cromfortune

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import yahoofinance.YahooFinance

open class CurrencyRateRetrievalCoroutineWorker(val context: Context, workerParameters: WorkerParameters) :
        CoroutineWorker(context, workerParameters) {

    companion object {

        private const val TAG = "CurrencyRateRetrievalCoroutineWorker"
        const val EXTRA_STRING_ARRAY_CURRENCIES = "${TAG}.EXTRA_STRING_ARRAY_CURRENCIES"

    }

    override suspend fun doWork(): Result = coroutineScope {
        Log.i(StockRetrievalCoroutineWorker.TAG, "doWork()")
        try {
            val cromFortuneApp = context.applicationContext as CromFortuneApp
            val asyncWork =
                    async {
                        cromFortuneApp.currencyRates["SEK"] = 1.0
                        for (currency in arrayOf("CAD", "EUR", "NOK", "USD")) {
                            cromFortuneApp.currencyRates[currency] = getRateInSek(currency)
                        }

                    }
            asyncWork.await()
            Result.success()
        } catch (error: Throwable) {
            Log.wtf(TAG, "Failed to retrieve currency rates", error)
            Result.failure()
        }
    }

    open fun getRateInSek(currency: String) = YahooFinance.getFx("${currency}SEK=X").price.toDouble()

}
