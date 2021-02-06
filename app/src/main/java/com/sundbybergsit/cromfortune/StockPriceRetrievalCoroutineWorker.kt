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
import java.util.*

@Suppress("BlockingMethodInNonBlockingContext")
class StockPriceRetrievalCoroutineWorker(val context: Context, workerParameters: WorkerParameters) :
        CoroutineWorker(context, workerParameters) {

    companion object {

        const val TAG = "StockRetrievalCoroutineWorker"

    }

    override suspend fun doWork(): Result = coroutineScope {
        Log.i(TAG, "doWork()")
        try {
            val asyncWork =
                    async {
                        val stocks: Map<String, Stock> = YahooFinance.get(StockPrice.SYMBOLS.map { pair -> pair.first }
                                .toTypedArray())
                        val stockPrices = mutableSetOf<StockPrice>()
                        for (triple in StockPrice.SYMBOLS.iterator()) {
                            val stockSymbol = triple.first
                            val quote = (stocks[stockSymbol] ?: error("")).getQuote(true)
                            val currency = triple.third
                            stockPrices.add(StockPrice(stockSymbol = stockSymbol, currency = Currency.getInstance(currency),
                                    price = quote.price.toDouble().roundTo(3)))
                        }
                        StockPriceRepository.put(stockPrices)
                    }
            asyncWork.await()
            Result.success()
        } catch (error: Throwable) {
            Result.failure()
        }
    }

}
