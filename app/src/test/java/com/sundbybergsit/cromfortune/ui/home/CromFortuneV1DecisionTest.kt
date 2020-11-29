package com.sundbybergsit.cromfortune.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.stocks.StocksPreferences
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
class CromFortuneV1DecisionTest {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var decision: CromFortuneV1Decision
    private val currencyConversionRateProducer = StubbedCurrencyConversionRateProducer()

    @Before
    fun setUp() {
        sharedPreferences = (ApplicationProvider.getApplicationContext() as Context)
                .getSharedPreferences(StocksPreferences.PREFERENCES_NAME,
                        Context.MODE_PRIVATE)
        decision = CromFortuneV1Decision(RuntimeEnvironment.systemContext, sharedPreferences)
    }

    @Test
    fun `getRecommendation - when stock price decreased to below limit and commission fee ok - returns buy recommendation`() {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("BUY", currency.toString(), 0L, DOMESTIC_STOCK_NAME, 100.0, 39.0, 10)
        sharedPreferences.edit().putStringSet(DOMESTIC_STOCK_NAME, setOf(Json.encodeToString(
                oldOrder))).commit()
        runBlocking {
            val recommendation: Recommendation? = decision.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME,
                    oldOrder.pricePerStock - (CromFortuneV1Decision.DIFF_PERCENTAGE + 0.1)
                            .times(oldOrder.pricePerStock)), 1.0, currencyConversionRateProducer)

            assertNotNull(recommendation)
            assertTrue(recommendation!!.command is BuyStockCommand)
        }
    }

    @Test
    fun `getRecommendation - when stock price increased to limit but commission fee too high - returns null`() {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("BUY", currency.toString(), 0L, DOMESTIC_STOCK_NAME, 100.0, 39.0, 1)
        sharedPreferences.edit().putStringSet(DOMESTIC_STOCK_NAME, setOf(Json.encodeToString(
                oldOrder))).commit()
        runBlocking {
            val recommendation = decision.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME,
                    oldOrder.pricePerStock + CromFortuneV1Decision.DIFF_PERCENTAGE.times(oldOrder.pricePerStock)), 1.0, currencyConversionRateProducer)

            assertNull(recommendation)
        }
    }

    @Test
    fun `getRecommendation - when stock price increased to above limit and commission fee ok but too few stocks - returns null`() {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("BUY", currency.toString(), 0L, DOMESTIC_STOCK_NAME, 100.0, 1.0, 1)
        sharedPreferences.edit().putStringSet(DOMESTIC_STOCK_NAME, setOf(Json.encodeToString(
                oldOrder))).commit()
        val newPrice = oldOrder.pricePerStock + (CromFortuneV1Decision.DIFF_PERCENTAGE + 0.1)
                .times(oldOrder.pricePerStock)

        runBlocking {
            val recommendation = decision.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME, newPrice), 1.0, currencyConversionRateProducer)

            assertNull(recommendation)
        }
    }

    @Test
    fun `getRecommendation - when stock price increased to above limit and commission fee ok but too few stocks - returns sell recommendation`() {
        val currency = Currency.getInstance("SEK")
        val oldOrder = StockOrder("BUY", currency.toString(), 0L, DOMESTIC_STOCK_NAME, 100.0, 10.0, 10)
        sharedPreferences.edit().putStringSet(DOMESTIC_STOCK_NAME, setOf(Json.encodeToString(
                oldOrder))).commit()
        val newPrice = oldOrder.pricePerStock + (CromFortuneV1Decision.DIFF_PERCENTAGE + 0.1)
                .times(oldOrder.pricePerStock)

        runBlocking {
            val recommendation = decision.getRecommendation(StockPrice(DOMESTIC_STOCK_NAME, newPrice), 10.0, currencyConversionRateProducer)

            assertNotNull(recommendation)
            assertTrue(recommendation!!.command is SellStockCommand)
        }
    }

    @Test
    fun `getRecommendation - when foreign stock price decreased to below limit and commission fee ok - returns buy recommendation`() {
        val currency = Currency.getInstance("NOK")
        val oldOrder = StockOrder("BUY", currency.toString(), 0L, FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, 10.0, 39.0, 10)
        sharedPreferences.edit().putStringSet(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, setOf(Json.encodeToString(
                oldOrder))).commit()
        runBlocking {
            val recommendation: Recommendation? = decision.getRecommendation(StockPrice(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME,
                    oldOrder.pricePerStock - (CromFortuneV1Decision.DIFF_PERCENTAGE + 0.1)
                            .times(oldOrder.pricePerStock)), 1.0, currencyConversionRateProducer)

            assertNotNull(recommendation)
            assertTrue(recommendation!!.command is BuyStockCommand)
        }
    }

    @Test
    fun `getRecommendation - when foreign stock price increased to limit but commission fee too high - returns null`() {
        val currency = Currency.getInstance("NOK")
        val oldOrder = StockOrder("BUY", currency.toString(), 0L, FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, 10.0, 39.0, 1)
        sharedPreferences.edit().putStringSet(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, setOf(Json.encodeToString(
                oldOrder))).commit()
        runBlocking {
            val recommendation = decision.getRecommendation(StockPrice(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME,
                    oldOrder.pricePerStock + CromFortuneV1Decision.DIFF_PERCENTAGE.times(oldOrder.pricePerStock)), 1.0, currencyConversionRateProducer)

            assertNull(recommendation)
        }
    }

    @Test
    fun `getRecommendation - when foreign stock price increased to above limit and commission fee ok but too few stocks - returns null`() {
        val currency = Currency.getInstance("NOK")
        val oldOrder = StockOrder("BUY", currency.toString(), 0L, FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, 10.0, 1.0, 1)
        sharedPreferences.edit().putStringSet(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, setOf(Json.encodeToString(
                oldOrder))).commit()
        val newPrice = oldOrder.pricePerStock + (CromFortuneV1Decision.DIFF_PERCENTAGE + 0.1)
                .times(oldOrder.pricePerStock)

        runBlocking {
            val recommendation = decision.getRecommendation(StockPrice(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, newPrice), 1.0, currencyConversionRateProducer)

            assertNull(recommendation)
        }
    }

    @Test
    fun `getRecommendation - when foreign stock price increased to above limit and commission fee ok but too few stocks - returns sell recommendation`() {
        val currency = Currency.getInstance("NOK")
        val oldOrder = StockOrder("BUY", currency.toString(), 0L, FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, 10.0, 10.0, 10)
        sharedPreferences.edit().putStringSet(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, setOf(Json.encodeToString(
                oldOrder))).commit()
        val newPrice = oldOrder.pricePerStock + (CromFortuneV1Decision.DIFF_PERCENTAGE + 0.1)
                .times(oldOrder.pricePerStock)

        runBlocking {
            val recommendation = decision.getRecommendation(StockPrice(FOREIGN_EXCHANGE_10X_SEK_STOCK_NAME, newPrice), 10.0, currencyConversionRateProducer)

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
