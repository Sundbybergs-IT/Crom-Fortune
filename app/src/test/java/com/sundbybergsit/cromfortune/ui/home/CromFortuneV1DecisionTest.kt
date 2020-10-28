package com.sundbybergsit.cromfortune.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.stocks.StocksPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.Q])
@RunWith(AndroidJUnit4::class)
class CromFortuneV1DecisionTest {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var decision: CromFortuneV1Decision

    @Before
    fun setUp() {
        sharedPreferences = (ApplicationProvider.getApplicationContext() as Context)
                .getSharedPreferences(StocksPreferences.PREFERENCES_NAME,
                        Context.MODE_PRIVATE)
        decision = CromFortuneV1Decision(RuntimeEnvironment.systemContext, sharedPreferences)
    }

    @Test
    fun `getRecommendation - when stock price decreased to below limit and commission fee ok - returns buy recommendation`() {
        val oldOrder = StockOrder("BUY", 0L, "Stock", 100.0, 39.0, 10)
        sharedPreferences.edit().putStringSet("Stock", setOf(Json.encodeToString(
                oldOrder))).commit()
        val recommendation: Recommendation? = decision.getRecommendation(StockPrice("Stock",
                oldOrder.pricePerStock - (CromFortuneV1Decision.DIFF_PERCENTAGE + 0.1)
                        .times(oldOrder.pricePerStock)), 1.0)

        assertNotNull(recommendation)
        assertTrue(recommendation!!.command is BuyStockCommand)
    }

    @Test
    fun `getRecommendation - when stock price increased to limit but commission fee too high - returns null`() {
        val oldOrder = StockOrder("BUY", 0L, "Stock", 100.0, 39.0, 1)
        sharedPreferences.edit().putStringSet("Stock", setOf(Json.encodeToString(
                oldOrder))).commit()
        val recommendation = decision.getRecommendation(StockPrice("Stock",
                oldOrder.pricePerStock + CromFortuneV1Decision.DIFF_PERCENTAGE.times(oldOrder.pricePerStock)), 1.0)

        assertNull(recommendation)
    }

    @Test
    fun `getRecommendation - when stock price increased to above limit and commission fee ok but too few stocks - returns null`() {
        val oldOrder = StockOrder("BUY", 0L, "Stock", 100.0, 1.0, 1)
        sharedPreferences.edit().putStringSet("Stock", setOf(Json.encodeToString(
                oldOrder))).commit()
        val newPrice = oldOrder.pricePerStock + (CromFortuneV1Decision.DIFF_PERCENTAGE + 0.1)
                .times(oldOrder.pricePerStock)

        val recommendation = decision.getRecommendation(StockPrice("Stock", newPrice), 1.0)

        assertNull(recommendation)
    }

    @Test
    fun `getRecommendation - when stock price increased to above limit and commission fee ok but too few stocks - returns sell recommendation`() {
        val oldOrder = StockOrder("BUY", 0L, "Stock", 100.0, 10.0, 10)
        sharedPreferences.edit().putStringSet("Stock", setOf(Json.encodeToString(
                oldOrder))).commit()
        val newPrice = oldOrder.pricePerStock + (CromFortuneV1Decision.DIFF_PERCENTAGE + 0.1)
                .times(oldOrder.pricePerStock)

        val recommendation = decision.getRecommendation(StockPrice("Stock", newPrice), 10.0)

        assertNotNull(recommendation)
        assertTrue(recommendation!!.command is SellStockCommand)
    }

}
