package com.sundbybergsit.cromfortune.main

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import com.sundbybergsit.cromfortune.domain.AssetCatalog
import com.sundbybergsit.cromfortune.domain.AssetPrice
import com.sundbybergsit.cromfortune.domain.TradableAsset
import com.sundbybergsit.cromfortune.main.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.main.stocks.StockPriceRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.Currency

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class StockDataRetrievalCoroutineWorkerTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @get:Rule
    val cromTestRule = CromTestRule()

    @Test
    fun `doWork - when market data retrieval succeeds - returns success`() {
        val client = FakeMarketDataClient()
        val worker = TestListenableWorkerBuilder<StockDataRetrievalCoroutineWorker>(context)
            .setWorkerFactory(StockRetrievalWorkerFactory(client))
            .build()
        runBlocking {
            val result: ListenableWorker.Result = worker.doWork()
            assertTrue(result == ListenableWorker.Result.success())
        }
        assertEquals(
            AssetCatalog.assets.map { it.quoteCurrency }.distinct().filterNot { it.currencyCode == "SEK" },
            client.requestedCurrencies
        )
        assertEquals(AssetCatalog.assets, client.requestedAssets)
        assertEquals(AssetCatalog.assets.map { it.id }.toSet(), StockPriceRepository.assetPricesStateFlow.value.assetPrices.map {
            it.assetId
        }.toSet())
        assertEquals(AssetCatalog.assets.map { it.quoteCurrency.currencyCode }.toSet(), CurrencyRateRepository.currencyRates.value.map {
            it.iso4217CurrencySymbol
        }.toSet())
    }

    @Test
    fun `doWork - when market data retrieval fails - returns failure`() {
        val client = FakeMarketDataClient(failure = IllegalStateException("Market data unavailable"))
        val worker = TestListenableWorkerBuilder<StockDataRetrievalCoroutineWorker>(context)
            .setWorkerFactory(StockRetrievalWorkerFactory(client))
            .build()

        runBlocking {
            val result: ListenableWorker.Result = worker.doWork()
            assertTrue(result == ListenableWorker.Result.failure())
        }
    }

    @Test
    fun `doWork - when one quote is missing - stores the remaining prices`() {
        val missingAsset = AssetCatalog.cryptocurrencies.first()
        val client = FakeMarketDataClient(missingAssetId = missingAsset.id)
        val worker = TestListenableWorkerBuilder<StockDataRetrievalCoroutineWorker>(context)
            .setWorkerFactory(StockRetrievalWorkerFactory(client))
            .build()

        runBlocking {
            assertEquals(ListenableWorker.Result.success(), worker.doWork())
        }
        assertEquals(null, StockPriceRepository.getAssetPrice(missingAsset.id))
        assertEquals(AssetCatalog.assets.size - 1, StockPriceRepository.assetPricesStateFlow.value.assetPrices.size)
    }

    private class FakeMarketDataClient(
        private val failure: Throwable? = null,
        private val missingAssetId: String? = null
    ) : MarketDataClient {

        val requestedCurrencies = mutableListOf<Currency>()
        var requestedAssets: List<TradableAsset> = emptyList()

        override fun getRateInSek(currency: Currency): Double {
            failure?.let { throw it }
            requestedCurrencies.add(currency)
            return 10.0
        }

        override fun getPrices(assets: Collection<TradableAsset>): Map<String, AssetPrice> {
            failure?.let { throw it }
            requestedAssets = assets.toList()
            return assets.filterNot { asset -> asset.id == missingAssetId }.associate { asset ->
                asset.id to AssetPrice(asset.id, asset.quoteCurrency, 100.0)
            }
        }
    }
}
