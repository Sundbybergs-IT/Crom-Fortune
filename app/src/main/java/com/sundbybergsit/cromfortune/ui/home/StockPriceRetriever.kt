package com.sundbybergsit.cromfortune.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

class StockPriceRetriever(private val stockPriceProducer: StockPriceProducer, private val interval: Long,
                          private val initialDelay: Long?) :
        CoroutineScope {

    private val _stockPrices = MutableLiveData<StockPrice>()
    private val allStockPrices: MutableSet<StockPrice> = mutableSetOf()

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

class StockPriceProducer {

    fun produce(): StockPrice {
        return StockPrice("Stock" + Random.nextInt(0, 20), Random.nextDouble())
    }

}
