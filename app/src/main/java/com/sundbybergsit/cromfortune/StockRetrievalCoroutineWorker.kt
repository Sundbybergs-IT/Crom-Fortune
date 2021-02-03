package com.sundbybergsit.cromfortune

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sundbybergsit.cromfortune.stocks.StockPriceRepository
import com.sundbybergsit.cromfortune.ui.home.StockPrice
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import yahoofinance.Stock
import yahoofinance.YahooFinance

@Suppress("BlockingMethodInNonBlockingContext")
class StockRetrievalCoroutineWorker(val context: Context, workerParameters: WorkerParameters) :
        CoroutineWorker(context, workerParameters) {

    companion object {

        const val TAG = "StockRetrievalCoroutineWorker"

    }

    override suspend fun doWork(): Result = coroutineScope {
        Log.i(TAG, "startWork()")
        try {
            val jobs =
                    async {
                        val stocks: Map<String, Stock> = YahooFinance.get(StockPrice.SYMBOLS.map { pair -> pair.first }
                                .toTypedArray())
                        val iterator = stocks.iterator()
                        val stockPrices = mutableSetOf<StockPrice>()
                        while (iterator.hasNext()) {
                            val stockSymbol = iterator.next().key
                            val quote = (stocks[stockSymbol] ?: error("")).getQuote(true)
                            stockPrices.add(StockPrice(stockSymbol, quote.price.toDouble().roundTo(3)))
                        }
                        StockPriceRepository.put(stockPrices)
                    }
            jobs.await()
            Result.success()
        } catch (error: Throwable) {
            Result.failure()
        }
    }

}
