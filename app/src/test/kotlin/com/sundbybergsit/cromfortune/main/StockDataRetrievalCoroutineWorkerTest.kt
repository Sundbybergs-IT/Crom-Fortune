package com.sundbybergsit.cromfortune.main

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.main.currencies.CurrencyRateRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class StockDataRetrievalCoroutineWorkerTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @get:Rule
    val cromTestRule = CromTestRule()

    @Test
    fun `doWork - when market data retrieval succeeds - returns success`() {
        val client = FakeStockMarketDataClient()
        val worker = TestListenableWorkerBuilder<StockDataRetrievalCoroutineWorker>(context)
            .setWorkerFactory(StockRetrievalWorkerFactory(client))
            .build()
        runBlocking {
            val result: ListenableWorker.Result = worker.doWork()
            assertTrue(result == ListenableWorker.Result.success())
        }
        assertEquals(StockPrice.CURRENCIES.filterNot { it == "SEK" }, client.requestedCurrencies)
        assertEquals(StockPrice.SYMBOLS.map { it.first }, client.requestedSymbols.toList())
        assertEquals(StockPrice.CURRENCIES.toSet(), CurrencyRateRepository.currencyRates.value.map {
            it.iso4217CurrencySymbol
        }.toSet())
    }

    @Test
    fun `doWork - when market data retrieval fails - returns failure`() {
        val client = FakeStockMarketDataClient(failure = IllegalStateException("Market data unavailable"))
        val worker = TestListenableWorkerBuilder<StockDataRetrievalCoroutineWorker>(context)
            .setWorkerFactory(StockRetrievalWorkerFactory(client))
            .build()

        runBlocking {
            val result: ListenableWorker.Result = worker.doWork()
            assertTrue(result == ListenableWorker.Result.failure())
        }
    }

    private class FakeStockMarketDataClient(
        private val failure: Throwable? = null
    ) : StockMarketDataClient {

        val requestedCurrencies = mutableListOf<String>()
        var requestedSymbols: Array<String> = emptyArray()

        override fun getRateInSek(currency: String): Double {
            failure?.let { throw it }
            requestedCurrencies.add(currency)
            return 10.0
        }

        override fun getStockPrices(symbols: Array<String>): Map<String, Double> {
            failure?.let { throw it }
            requestedSymbols = symbols
            return emptyMap()
        }
    }
}
