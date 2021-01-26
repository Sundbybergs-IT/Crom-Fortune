package com.sundbybergsit.cromfortune.ui.home

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.stocks.StockOrderRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.util.*

private const val DOMESTIC_STOCK_NAME = "Aktie med normal valutakurs"
private const val FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME = "Aktie med annan valutakurs"

@Config(sdk = [Build.VERSION_CODES.Q])
@RunWith(AndroidJUnit4::class)
class CromFortuneV1RecommendationAlgorithmTest {

    private lateinit var algorithm: CromFortuneV1RecommendationAlgorithm

    private lateinit var repository: StockOrderRepository
    private val currencyConversionRateProducer = StubbedCurrencyConversionRateProducer()

    @Before
    fun setUp() {
        repository = StockOrderRepositoryImpl(ApplicationProvider.getApplicationContext() as Context)
        algorithm = CromFortuneV1RecommendationAlgorithm(RuntimeEnvironment.systemContext, repository)
    }

    @Test
    fun `getRecommendation - when stock price decreased to below limit and commission fee ok - returns buy recommendation`() {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("BUY", currency.toString(), 0L, DOMESTIC_STOCK_NAME, 100.0, 39.0, 10)
        repository.put(DOMESTIC_STOCK_NAME, oldOrder)
        runBlocking {
            val recommendation: Recommendation? = algorithm.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME,
                    oldOrder.pricePerStock - (CromFortuneV1RecommendationAlgorithm.DIFF_PERCENTAGE + 0.1)
                            .times(oldOrder.pricePerStock)), 1.0, currencyConversionRateProducer, setOf(oldOrder))

            assertNotNull(recommendation)
            assertTrue(recommendation!!.command is BuyStockCommand)
        }
    }

    @Test
    fun `getRecommendation - when stock price increased to limit but commission fee too high - returns null`() {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("BUY", currency.toString(), 0L, DOMESTIC_STOCK_NAME, 100.0, 39.0, 1)
        repository.put(DOMESTIC_STOCK_NAME, oldOrder)
        runBlocking {
            val recommendation = algorithm.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME,
                    oldOrder.pricePerStock + CromFortuneV1RecommendationAlgorithm.DIFF_PERCENTAGE.times(oldOrder.pricePerStock)),
                    1.0, currencyConversionRateProducer, setOf(oldOrder))

            assertNull(recommendation)
        }
    }

    @Test
    fun `getRecommendation - when stock price increased to above limit and commission fee ok but too few stocks - returns null`() {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("BUY", currency.toString(), 0L, DOMESTIC_STOCK_NAME, 100.0, 1.0, 1)
        repository.put(DOMESTIC_STOCK_NAME, oldOrder)
        val newPrice = oldOrder.pricePerStock + (CromFortuneV1RecommendationAlgorithm.DIFF_PERCENTAGE + 0.1)
                .times(oldOrder.pricePerStock)

        runBlocking {
            val recommendation = algorithm.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME, newPrice), 1.0,
                    currencyConversionRateProducer, setOf(oldOrder))

            assertNull(recommendation)
        }
    }

    @Test
    fun `getRecommendation - when stock price increased to above limit and commission fee ok but too few stocks - returns sell recommendation`() {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("BUY", currency.toString(), 0L, DOMESTIC_STOCK_NAME, 100.0, 10.0, 10)
        repository.put(DOMESTIC_STOCK_NAME, oldOrder)
        val newPrice = oldOrder.pricePerStock + (CromFortuneV1RecommendationAlgorithm.DIFF_PERCENTAGE + 0.1)
                .times(oldOrder.pricePerStock)

        runBlocking {
            val recommendation = algorithm.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME, newPrice), 10.0,
                    currencyConversionRateProducer, setOf(oldOrder))

            assertNotNull(recommendation)
            assertTrue(recommendation!!.command is SellStockCommand)
        }
    }

    @Test
    fun `getRecommendation - when foreign stock price decreased to below limit and commission fee ok - returns buy recommendation`() {
        val currency = Currency.getInstance("NOK")
        val oldOrder = StockOrder("BUY", currency.toString(), 0L, FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, 10.0, 39.0, 10)
        repository.put(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, oldOrder)
        runBlocking {
            val recommendation: Recommendation? = algorithm.getRecommendation(StockPrice(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME,
                    oldOrder.pricePerStock - (CromFortuneV1RecommendationAlgorithm.DIFF_PERCENTAGE + 0.1)
                            .times(oldOrder.pricePerStock)), 1.0,
                    currencyConversionRateProducer, setOf(oldOrder))

            assertNotNull(recommendation)
            assertTrue(recommendation!!.command is BuyStockCommand)
        }
    }

    @Test
    fun `getRecommendation - when foreign stock price increased to limit but commission fee too high - returns null`() {
        val currency = Currency.getInstance("NOK")
        val oldOrder = StockOrder("BUY", currency.toString(), 0L, FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, 10.0, 39.0, 1)
        repository.put(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, oldOrder)
        runBlocking {
            val recommendation = algorithm.getRecommendation(StockPrice(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME,
                    oldOrder.pricePerStock + CromFortuneV1RecommendationAlgorithm.DIFF_PERCENTAGE.times(oldOrder.pricePerStock)),
                    1.0, currencyConversionRateProducer, setOf(oldOrder))

            assertNull(recommendation)
        }
    }

    @Test
    fun `getRecommendation - when foreign stock price increased to above limit and commission fee ok but too few stocks - returns null`() {
        val currency = Currency.getInstance("NOK")
        val oldOrder = StockOrder("BUY", currency.toString(), 0L, FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, 10.0, 1.0, 1)
        repository.put(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, oldOrder)
        val newPrice = oldOrder.pricePerStock + (CromFortuneV1RecommendationAlgorithm.DIFF_PERCENTAGE + 0.1)
                .times(oldOrder.pricePerStock)

        runBlocking {
            val recommendation = algorithm.getRecommendation(StockPrice(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, newPrice),
                    1.0, currencyConversionRateProducer, setOf(oldOrder))

            assertNull(recommendation)
        }
    }

    @Test
    fun `getRecommendation - when foreign stock price increased to above limit and commission fee ok but too few stocks - returns sell recommendation`() {
        val currency = Currency.getInstance("NOK")
        val oldOrder = StockOrder("BUY", currency.toString(), 0L, FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, 10.0, 10.0, 10)
        repository.put(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, oldOrder)
        val newPrice = oldOrder.pricePerStock + (CromFortuneV1RecommendationAlgorithm.DIFF_PERCENTAGE + 0.1)
                .times(oldOrder.pricePerStock)

        runBlocking {
            val recommendation = algorithm.getRecommendation(StockPrice(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, newPrice),
                    10.0, currencyConversionRateProducer, setOf(oldOrder))

            assertNotNull(recommendation)
            assertTrue(recommendation!!.command is SellStockCommand)
        }
    }

    class StubbedCurrencyConversionRateProducer : CurrencyConversionRateProducer() {

        override fun getRateInSek(stockSymbol: String) = when (stockSymbol) {
            DOMESTIC_STOCK_NAME -> 1.0
            FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME -> 10.0
            else -> throw UnsupportedOperationException()
        }

    }

}