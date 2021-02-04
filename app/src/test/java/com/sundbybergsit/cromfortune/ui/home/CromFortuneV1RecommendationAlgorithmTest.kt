package com.sundbybergsit.cromfortune.ui.home

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.stocks.StockOrderRepository
import com.sundbybergsit.cromfortune.stocks.StockOrderRepositoryImpl
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
        algorithm = CromFortuneV1RecommendationAlgorithm(RuntimeEnvironment.systemContext)
    }

    @Test
    fun `getRecommendation - when stock price decreased to below limit and commission fee ok - returns buy recommendation`() {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, DOMESTIC_STOCK_NAME, 100.0, 39.0, 10)
        repository.put(DOMESTIC_STOCK_NAME, oldOrder)
        runBlocking {
            val recommendation: Recommendation? = algorithm.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME,
                    oldOrder.pricePerStock - (CromFortuneV1RecommendationAlgorithm.DIFF_PERCENTAGE + 0.1)
                            .times(oldOrder.pricePerStock)), 1.0, currencyConversionRateProducer, setOf(oldOrder))

            assertNotNull(recommendation)
            assertTrue(recommendation!!.command is BuyStockCommand)
            val buyStockCommand = recommendation.command as BuyStockCommand
            assertTrue(buyStockCommand.commissionFee == 1.0)
            assertTrue(buyStockCommand.quantity == 1)
            assertTrue(buyStockCommand.pricePerStock == 80.0)
            assertTrue(buyStockCommand.currency == currency)
        }
    }

    @Test
    fun `getRecommendation - when stock price decreased to below limit and commission fee ok including sell - returns buy recommendation`() {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, DOMESTIC_STOCK_NAME, 100.0, 39.0, 11)
        val oldOrder2 = StockOrder("Sell", currency.toString(), 0L, DOMESTIC_STOCK_NAME, 100.0, 1000.0, 1)
        repository.putAll(DOMESTIC_STOCK_NAME, setOf(oldOrder, oldOrder2))
        runBlocking {
            val recommendation: Recommendation? = algorithm.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME,
                    oldOrder.pricePerStock - (CromFortuneV1RecommendationAlgorithm.DIFF_PERCENTAGE + 0.1)
                            .times(oldOrder.pricePerStock)), 1.0, currencyConversionRateProducer, setOf(oldOrder))

            assertNotNull(recommendation)
            assertTrue(recommendation!!.command is BuyStockCommand)
            val buyStockCommand = recommendation.command as BuyStockCommand
            assertTrue(buyStockCommand.commissionFee == 1.0)
            assertTrue(buyStockCommand.quantity == 1)
            assertTrue(buyStockCommand.pricePerStock == 80.0)
            assertTrue(buyStockCommand.currency == currency)
        }
    }

    @Test
    fun `getRecommendation - when stock price increased to limit but buy commission fee too high - returns null`() {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, DOMESTIC_STOCK_NAME, 100.0, 39.0, 1)
        repository.put(DOMESTIC_STOCK_NAME, oldOrder)
        runBlocking {
            val recommendation = algorithm.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME,
                    oldOrder.pricePerStock + CromFortuneV1RecommendationAlgorithm.DIFF_PERCENTAGE.times(oldOrder.pricePerStock)),
                    1.0, currencyConversionRateProducer, setOf(oldOrder))

            assertNull(recommendation)
        }
    }

    @Test
    fun `getRecommendation - when stock price increased to limit but sell commission fee too high - returns null`() {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, DOMESTIC_STOCK_NAME, 100.0, 1.0, 1)
        repository.put(DOMESTIC_STOCK_NAME, oldOrder)
        runBlocking {
            val recommendation = algorithm.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME,
                    oldOrder.pricePerStock + CromFortuneV1RecommendationAlgorithm.DIFF_PERCENTAGE.times(oldOrder.pricePerStock)),
                    39.0, currencyConversionRateProducer, setOf(oldOrder))

            assertNull(recommendation)
        }
    }

    @Test
    fun `getRecommendation - when stock price increased to above limit and commission fee ok but too few stocks - returns null`() {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, DOMESTIC_STOCK_NAME, 100.0, 1.0, 1)
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
    fun `getRecommendation - when stock price increased to above limit and commission fee ok and enough stocks - returns sell recommendation`() {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, DOMESTIC_STOCK_NAME, 100.0, 10.0, 10)
        repository.put(DOMESTIC_STOCK_NAME, oldOrder)
        val newPrice = oldOrder.pricePerStock + (CromFortuneV1RecommendationAlgorithm.DIFF_PERCENTAGE + 0.1)
                .times(oldOrder.pricePerStock)

        runBlocking {
            val recommendation = algorithm.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME, newPrice), 0.0,
                    currencyConversionRateProducer, setOf(oldOrder))

            assertNotNull(recommendation)
            assertTrue(recommendation!!.command is SellStockCommand)
            val sellStockCommand = recommendation.command as SellStockCommand
            assertTrue(sellStockCommand.commissionFee == 0.0)
            assertTrue(sellStockCommand.quantity == 1)
            assertTrue(sellStockCommand.pricePerStock == 120.0)
            assertTrue(sellStockCommand.currency == currency)
        }
    }

    @Test
    fun `getRecommendation - when foreign stock price decreased to below limit and commission fee ok - returns buy recommendation`() {
        val currency = Currency.getInstance("NOK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, 10.0, 39.0, 10)
        repository.put(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, oldOrder)
        runBlocking {
            val recommendation: Recommendation? = algorithm.getRecommendation(StockPrice(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME,
                    oldOrder.pricePerStock - (CromFortuneV1RecommendationAlgorithm.DIFF_PERCENTAGE + 0.1)
                            .times(oldOrder.pricePerStock)), 1.0,
                    currencyConversionRateProducer, setOf(oldOrder))

            assertNotNull(recommendation)
            assertTrue(recommendation!!.command is BuyStockCommand)
            val buyStockCommand = recommendation.command as BuyStockCommand
            assertTrue(buyStockCommand.commissionFee == 1.0)
            assertTrue(buyStockCommand.quantity == 1)
            assertTrue(buyStockCommand.pricePerStock == 8.0)
            assertTrue(buyStockCommand.currency == currency)
        }
    }

    @Test
    fun `getRecommendation - when foreign stock price increased to over limit and including sell - returns buy recommendation`() {
        val currency = Currency.getInstance("NOK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, 9.0, 39.0, 70)
        val oldOrder2 = StockOrder("Sell", currency.toString(), 0L, FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, 1000.0, 39.0, 35)
        repository.putAll(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, setOf(oldOrder, oldOrder2))
        runBlocking {
            val recommendation: Recommendation? = algorithm.getRecommendation(StockPrice(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME,
                    oldOrder.pricePerStock + (CromFortuneV1RecommendationAlgorithm.DIFF_PERCENTAGE + 0.09)
                            .times(oldOrder.pricePerStock)), 39.0,
                    currencyConversionRateProducer, setOf(oldOrder, oldOrder2))

            assertNull(recommendation)
        }
    }

    @Test
    fun `getRecommendation - when foreign stock price increased to limit but commission fee too high - returns null`() {
        val currency = Currency.getInstance("NOK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, 10.0, 39.0, 1)
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
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, 10.0, 1.0, 1)
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
    fun `getRecommendation - when foreign stock price increased to above limit and commission fee and enough stocks - returns sell recommendation`() {
        val currency = Currency.getInstance("NOK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, 10.0, 10.0, 10)
        repository.put(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, oldOrder)
        val newPrice = oldOrder.pricePerStock + (CromFortuneV1RecommendationAlgorithm.DIFF_PERCENTAGE + 0.1)
                .times(oldOrder.pricePerStock)

        runBlocking {
            val recommendation = algorithm.getRecommendation(StockPrice(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, newPrice),
                    0.0, currencyConversionRateProducer, setOf(oldOrder))

            assertNotNull(recommendation)
            assertTrue(recommendation!!.command is SellStockCommand)
            val sellStockCommand = recommendation.command as SellStockCommand
            assertTrue(sellStockCommand.commissionFee == 0.0)
            assertTrue(sellStockCommand.quantity == 1)
            assertTrue(sellStockCommand.pricePerStock == 12.0)
            assertTrue(sellStockCommand.currency == currency)
        }
    }

    class StubbedCurrencyConversionRateProducer : CurrencyConversionRateProducer(ApplicationProvider.getApplicationContext()) {

        override fun getRateInSek(currency: Currency) = when (currency) {
            Currency.getInstance("SEK") -> 1.0
            Currency.getInstance("NOK") -> 10.0
            else -> throw UnsupportedOperationException()
        }

    }

}
