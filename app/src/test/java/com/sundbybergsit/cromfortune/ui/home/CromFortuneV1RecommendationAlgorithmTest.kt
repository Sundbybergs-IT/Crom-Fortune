package com.sundbybergsit.cromfortune.ui.home

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.currencies.CurrencyRate
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.stocks.StockOrder
import com.sundbybergsit.cromfortune.stocks.StockOrderRepository
import com.sundbybergsit.cromfortune.stocks.StockOrderRepositoryImpl
import com.sundbybergsit.cromfortune.stocks.StockPrice
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.util.*

private const val DOMESTIC_STOCK_NAME = "Aktie med normal valutakurs"
private const val FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME = "Aktie med annan valutakurs"

@Config(sdk = [Build.VERSION_CODES.Q])
@RunWith(AndroidJUnit4::class)
class CromFortuneV1RecommendationAlgorithmTest {

    private lateinit var algorithm: CromFortuneV1RecommendationAlgorithm

    private lateinit var repository: StockOrderRepository

    @Before
    fun setUp() {
        CurrencyRateRepository.add(setOf(
                CurrencyRate("SEK", 1.0),
                CurrencyRate("NOK", 10.0))
        )
        ShadowLooper.runUiThreadTasks()
        repository = StockOrderRepositoryImpl(ApplicationProvider.getApplicationContext() as Context)
        algorithm = CromFortuneV1RecommendationAlgorithm(RuntimeEnvironment.systemContext)
    }

    @Test
    fun `getRecommendation - when stock price decreased to below normal limit and commission fee ok and overbought - returns no recommendation`() = runBlocking {
        val currency = Currency.getInstance("SEK")
        val oldOrder1 = StockOrder("Buy", currency.toString(), 0L, DOMESTIC_STOCK_NAME,
                100.0, 39.0, 2)
        val oldOrder2 = StockOrder("Sell", currency.toString(), 0L, DOMESTIC_STOCK_NAME,
                100.0, 39.0, 2)
        val oldOrder3 = StockOrder("Buy", currency.toString(), 0L, DOMESTIC_STOCK_NAME,
                100.0, 39.0, 10)
        repository.putAll(DOMESTIC_STOCK_NAME, setOf(oldOrder1, oldOrder2, oldOrder3))

        val recommendation: Recommendation? = algorithm.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME, currency,
                oldOrder3.pricePerStock - (CromFortuneV1RecommendationAlgorithm.NORMAL_DIFF_PERCENTAGE + 0.1)
                        .times(oldOrder3.pricePerStock)), 1.0, 1.0, setOf(oldOrder1, oldOrder2, oldOrder3))

        assertNull(recommendation)
    }

    @Test
    fun `getRecommendation - when stock price decreased to below normal limit and commission fee ok - returns buy recommendation`() = runBlocking {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, DOMESTIC_STOCK_NAME,
                100.0, 39.0, 10)
        repository.put(DOMESTIC_STOCK_NAME, oldOrder)

        val recommendation: Recommendation? = algorithm.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME, currency,
                oldOrder.pricePerStock - (CromFortuneV1RecommendationAlgorithm.NORMAL_DIFF_PERCENTAGE + 0.1)
                        .times(oldOrder.pricePerStock)), 1.0, 1.0, setOf(oldOrder))

        assertNotNull(recommendation)
        assertTrue(recommendation!!.command is BuyStockCommand)
        val buyStockCommand = recommendation.command as BuyStockCommand
        assertTrue(buyStockCommand.commissionFee == 1.0)
        assertQuantity(2, buyStockCommand.quantity)
        assertStockPrice(70.0, buyStockCommand.pricePerStock)
        assertTrue(buyStockCommand.currency == currency)
    }

    @Test
    fun `getRecommendation - when stock price decreased to below high limit and commission fee ok and not overbought - returns no recommendation`() = runBlocking {
        val currency = Currency.getInstance("SEK")
        val oldOrder1 = StockOrder("Buy", currency.toString(), 0L, DOMESTIC_STOCK_NAME,
                100.0, 39.0, 300)
        val oldOrder2 = StockOrder("Sell", currency.toString(), 1L, DOMESTIC_STOCK_NAME,
                100.0, 39.0, 30)
        val oldOrder3 = StockOrder("Buy", currency.toString(), 2L, DOMESTIC_STOCK_NAME,
                100.0, 39.0, 10)
        repository.putAll(DOMESTIC_STOCK_NAME, setOf(oldOrder1, oldOrder2, oldOrder3))

        val recommendation: Recommendation? = algorithm.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME, currency,
                oldOrder3.pricePerStock - (CromFortuneV1RecommendationAlgorithm.MAX_EXTREME_BUY_PERCENTAGE + 0.1)
                        .times(oldOrder3.pricePerStock)), 1.0, 1.0, setOf(oldOrder1, oldOrder2, oldOrder3))

        assertNotNull(recommendation)
        assertTrue(recommendation!!.command is BuyStockCommand)
        val buyStockCommand = recommendation.command as BuyStockCommand
        assertTrue(buyStockCommand.commissionFee == 1.0)
        assertQuantity(32, buyStockCommand.quantity)
        assertStockPrice(70.0, buyStockCommand.pricePerStock)
        assertTrue(buyStockCommand.currency == currency)
    }

    @Test
    fun `getRecommendation - when stock price decreased to below limit and commission fee ok including sell - returns buy recommendation`() = runBlocking {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, DOMESTIC_STOCK_NAME,
                100.0, 39.0, 11)
        val oldOrder2 = StockOrder("Sell", currency.toString(), 0L, DOMESTIC_STOCK_NAME,
                100.0, 1000.0, 1)
        repository.putAll(DOMESTIC_STOCK_NAME, setOf(oldOrder, oldOrder2))

        val recommendation: Recommendation? = algorithm.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME, currency,
                oldOrder.pricePerStock - (CromFortuneV1RecommendationAlgorithm.NORMAL_DIFF_PERCENTAGE + 0.1)
                        .times(oldOrder.pricePerStock)), 1.0, 1.0, setOf(oldOrder))

        assertNotNull(recommendation)
        assertTrue(recommendation!!.command is BuyStockCommand)
        val buyStockCommand = recommendation.command as BuyStockCommand
        assertTrue(buyStockCommand.commissionFee == 1.0)
        assertQuantity(2, buyStockCommand.quantity)
        assertStockPrice(70.0, buyStockCommand.pricePerStock)
        assertTrue(buyStockCommand.currency == currency)
    }

    @Test
    fun `getRecommendation - when stock price increased to limit but buy commission fee too high - returns null`() = runBlocking {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, DOMESTIC_STOCK_NAME,
                100.0, 39.0, 1)
        repository.put(DOMESTIC_STOCK_NAME, oldOrder)

        val recommendation = algorithm.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME, currency,
                oldOrder.pricePerStock + CromFortuneV1RecommendationAlgorithm.NORMAL_DIFF_PERCENTAGE
                        .times(oldOrder.pricePerStock)),
                1.0, 1.0, setOf(oldOrder))

        assertNull(recommendation)
    }

    @Test
    fun `getRecommendation - when stock price increased to limit but sell commission fee too high - returns null`() = runBlocking {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, DOMESTIC_STOCK_NAME, 100.0,
                1.0, 1)
        repository.put(DOMESTIC_STOCK_NAME, oldOrder)

        val recommendation = algorithm.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME, currency,
                oldOrder.pricePerStock + CromFortuneV1RecommendationAlgorithm.NORMAL_DIFF_PERCENTAGE
                        .times(oldOrder.pricePerStock)),
                1.0, 39.0, setOf(oldOrder))

        assertNull(recommendation)
    }

    @Test
    fun `getRecommendation - when stock price increased to max limit - returns null`() = runBlocking {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, DOMESTIC_STOCK_NAME, 100.0,
                1.0, 10)
        val oldOrder2 = StockOrder("Sell", currency.toString(), 1L, DOMESTIC_STOCK_NAME, 200.0,
                1.0, 5)
        repository.putAll(DOMESTIC_STOCK_NAME, setOf(oldOrder, oldOrder2))

        val recommendation = algorithm.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME, currency,
                oldOrder.pricePerStock + 100
                        .times(oldOrder.pricePerStock)),
                1.0, 39.0, setOf(oldOrder, oldOrder2))

        assertNull(recommendation)
    }

    @Test
    fun `getRecommendation - when stock price increased to above limit and commission fee ok but too few stocks - returns null`() = runBlocking {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, DOMESTIC_STOCK_NAME, 100.0,
                1.0, 1)
        repository.put(DOMESTIC_STOCK_NAME, oldOrder)
        val newPrice = oldOrder.pricePerStock + (CromFortuneV1RecommendationAlgorithm.NORMAL_DIFF_PERCENTAGE + 0.1)
                .times(oldOrder.pricePerStock)

        val recommendation = algorithm.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME, currency, newPrice),
                1.0, 1.0, setOf(oldOrder))

        assertNull(recommendation)
    }

    @Test
    fun `getRecommendation - when stock price increased to above limit and commission fee ok and enough stocks - returns sell recommendation`() = runBlocking {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, DOMESTIC_STOCK_NAME,
                100.0, 10.0, 13)
        repository.put(DOMESTIC_STOCK_NAME, oldOrder)
        val newPrice = oldOrder.pricePerStock + (CromFortuneV1RecommendationAlgorithm.NORMAL_DIFF_PERCENTAGE + 0.1)
                .times(oldOrder.pricePerStock)

        val recommendation = algorithm.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME, currency, newPrice),
                1.0, 0.0, setOf(oldOrder))

        assertNotNull(recommendation)
        assertTrue(recommendation!!.command is SellStockCommand)
        val sellStockCommand = recommendation.command as SellStockCommand
        assertTrue(sellStockCommand.commissionFee == 0.0)
        assertQuantity(9, sellStockCommand.quantity)
        assertStockPrice(130.0, sellStockCommand.pricePerStock)
        assertTrue(sellStockCommand.currency == currency)
    }

    @Test
    fun `getRecommendation - when foreign stock price decreased to below limit and commission fee ok - returns buy recommendation`() = runBlocking {
        val currency = Currency.getInstance("NOK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME,
                10.0, 39.0, 10)
        repository.put(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, oldOrder)

        val recommendation: Recommendation? = algorithm.getRecommendation(StockPrice(
                FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, currency,
                oldOrder.pricePerStock - (CromFortuneV1RecommendationAlgorithm.NORMAL_DIFF_PERCENTAGE + 0.1)
                        .times(oldOrder.pricePerStock)), 10.0, 1.0, setOf(oldOrder))

        assertNotNull(recommendation)
        assertTrue(recommendation!!.command is BuyStockCommand)
        val buyStockCommand = recommendation.command as BuyStockCommand
        assertTrue(buyStockCommand.commissionFee == 1.0)
        assertQuantity(2, buyStockCommand.quantity)
        assertStockPrice(7.0, buyStockCommand.pricePerStock)
        assertTrue(buyStockCommand.currency == currency)
    }

    @Test
    fun `getRecommendation - when foreign stock price increased to over limit and including sell - returns buy recommendation`() = runBlocking {
        val currency = Currency.getInstance("NOK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME,
                9.0, 39.0, 70)
        val oldOrder2 = StockOrder("Sell", currency.toString(), 1L, FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME,
                1000.0, 39.0, 35)
        repository.putAll(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, setOf(oldOrder, oldOrder2))

        val recommendation: Recommendation? = algorithm.getRecommendation(
                StockPrice(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, currency,
                        oldOrder.pricePerStock + (CromFortuneV1RecommendationAlgorithm.NORMAL_DIFF_PERCENTAGE + 0.09)
                                .times(oldOrder.pricePerStock)), 10.0, 39.0, setOf(oldOrder, oldOrder2))

        assertNull(recommendation)
    }

    @Test
    fun `getRecommendation - when foreign stock price increased to limit but commission fee too high - returns null`() = runBlocking {
        val currency = Currency.getInstance("NOK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, 10.0, 39.0, 1)
        repository.put(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, oldOrder)
        val recommendation = algorithm.getRecommendation(StockPrice(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, currency,
                oldOrder.pricePerStock + CromFortuneV1RecommendationAlgorithm.NORMAL_DIFF_PERCENTAGE.times(
                        oldOrder.pricePerStock)), 10.0, 1.0, setOf(oldOrder))

        assertNull(recommendation)
    }

    @Test
    fun `getRecommendation - when foreign stock price increased to above limit and commission fee ok but too few stocks - returns null`() = runBlocking {
        val currency = Currency.getInstance("NOK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME,
                10.0, 1.0, 1)
        repository.put(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, oldOrder)
        val newPrice = oldOrder.pricePerStock + (CromFortuneV1RecommendationAlgorithm.NORMAL_DIFF_PERCENTAGE + 0.1)
                .times(oldOrder.pricePerStock)

        val recommendation = algorithm.getRecommendation(StockPrice(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, currency,
                newPrice), 10.0, 1.0, setOf(oldOrder))

        assertNull(recommendation)
    }

    @Test
    fun `getRecommendation - when foreign stock price increased to above limit and commission fee and enough stocks - returns sell recommendation`() = runBlocking {
        val currency = Currency.getInstance("NOK")
        val oldOrder = StockOrder("Buy", currency.toString(), 0L, FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME,
                10.0, 10.0, 10)
        repository.put(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, oldOrder)
        val newPrice = oldOrder.pricePerStock + (CromFortuneV1RecommendationAlgorithm.NORMAL_DIFF_PERCENTAGE + 0.1)
                .times(oldOrder.pricePerStock)

        val recommendation = algorithm.getRecommendation(StockPrice(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, currency,
                newPrice), 10.0, 0.0, setOf(oldOrder))

        assertNotNull(recommendation)
        assertTrue(recommendation!!.command is SellStockCommand)
        val sellStockCommand = recommendation.command as SellStockCommand
        assertTrue(sellStockCommand.commissionFee == 0.0)
        assertQuantity(7, sellStockCommand.quantity)
        assertStockPrice(13.0, sellStockCommand.pricePerStock)
        assertTrue(sellStockCommand.currency == currency)
    }

    private fun assertStockPrice(expected: Double, actual: Double) {
        assertTrue("Expected stock price to be $expected but was $actual", actual == expected)
    }

    private fun assertQuantity(expected: Int, actual: Int) {
        assertTrue("Expected quantity $expected but was $actual", actual == expected)
    }

}
