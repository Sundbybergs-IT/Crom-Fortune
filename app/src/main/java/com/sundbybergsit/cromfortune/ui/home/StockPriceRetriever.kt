package com.sundbybergsit.cromfortune.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import yahoofinance.Stock
import yahoofinance.YahooFinance
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

class StockPriceRetriever(private val stockPriceProducer: StockPriceProducer, private val interval: Long,
                          private val initialDelay: Long?) :
        CoroutineScope {

    private val _stockPrices = MutableLiveData<StockPrice>()
    private val allStockPrices: MutableSet<StockPrice> = mutableSetOf()
    private val symbols = arrayOf("MIPS.ST", "ASSA-B.ST", "AZELIO.ST")

    val stockPrices: LiveData<StockPrice> = _stockPrices

    private val job = Job()
    private val singleThreadExecutor = Executors.newSingleThreadExecutor()

    override val coroutineContext: CoroutineContext
        get() = job + singleThreadExecutor.asCoroutineDispatcher()

    fun stop() {
        job.cancel()
        singleThreadExecutor.shutdown()
    }

    fun start() = launch {
        initialDelay?.let {
            delay(it)
        }
        GlobalScope.launch(Dispatchers.IO) {
            StockPriceProducer.stocks = YahooFinance.get(symbols)
            while (isActive) {
                val newStockPrice = stockPriceProducer.produce()
                Log.i("StockPriceRetriever", "New stock price: $newStockPrice")
                allStockPrices.add(newStockPrice)
                _stockPrices.postValue(newStockPrice)
                delay(interval)
            }
            println("coroutine done")
        }
    }

}

class StockPriceProducer {

    companion object {

        lateinit var stocks: Map<String, Stock>
        var iterator: Iterator<Map.Entry<String, Stock>> = mapOf<String, Stock>().iterator()

    }

    @Synchronized
    fun produce(): StockPrice {
        if (!iterator.hasNext()) {
            iterator = stocks.iterator()
        }
        val stockSymbol = iterator.next().key
        val quote = (stocks[stockSymbol] ?: error("")).getQuote(true)
        return StockPrice(stockSymbol, quote.price.toDouble())
    }

}