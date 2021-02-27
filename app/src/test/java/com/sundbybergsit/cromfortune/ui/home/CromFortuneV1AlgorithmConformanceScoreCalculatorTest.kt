package com.sundbybergsit.cromfortune.ui.home

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.currencies.CurrencyRate
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.stocks.StockOrder
import com.sundbybergsit.cromfortune.stocks.StockPrice
import com.sundbybergsit.cromfortune.stocks.StockPriceRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class CromFortuneV1AlgorithmConformanceScoreCalculatorTest {

    private lateinit var calculator: CromFortuneV1AlgorithmConformanceScoreCalculator

    @Before
    fun setUp() {
        CurrencyRateRepository.add(setOf(CurrencyRate("SEK", 1.0)))
        StockPriceRepository.put(setOf(
                StockPrice(StockPrice.SYMBOLS[0].first, Currency.getInstance("SEK"), 1.0)
        ))
        ShadowLooper.runUiThreadTasks()
        calculator = CromFortuneV1AlgorithmConformanceScoreCalculator()
    }

    @Test
    fun `getScore - when no orders - returns 100`() = runBlocking {
        val score = calculator.getScore(SellRecommendationDummyAlgorithm(), emptySet(), CurrencyRateRepository)

        assertScore(100, score)
    }

    @Test(expected = IllegalStateException::class)
    fun `getScore - when initial sell order - throws exception`() {
        runBlocking {
            calculator.getScore(SellRecommendationDummyAlgorithm(), setOf(newSellStockOrder(1)), CurrencyRateRepository)
        }
    }

    @Test
    fun `getScore - when initial buy order - returns 100`() = runBlocking {
        val score = calculator.getScore(SellRecommendationDummyAlgorithm(), setOf(newBuyStockOrder(1)), CurrencyRateRepository)

        assertScore(100, score)
    }

    @Test
    fun `getScore - when 1 out of 2 correct decisions - returns 50`() = runBlocking {
        val score = calculator.getScore(SellRecommendationDummyAlgorithm(), setOf(newBuyStockOrder(1),
                newBuyStockOrder(2)), CurrencyRateRepository)

        assertScore(50, score)
    }

    @Test
    fun `getScore - when 2 out of 2 correct decisions - returns 100`() = runBlocking {
        val score = calculator.getScore(SellRecommendationDummyAlgorithm(), setOf(newBuyStockOrder(1),
                newSellStockOrder(2)), CurrencyRateRepository)

        assertScore(100, score)
    }

    @Test
    fun `getScore - when 2 out of 3 correct decisions - returns 66`() = runBlocking {
        val score = calculator.getScore(SellRecommendationDummyAlgorithm(), setOf(newBuyStockOrder(1),
                newSellStockOrder(2), newBuyStockOrder(3)), CurrencyRateRepository)

        assertScore(66, score)
    }

    @Test
    fun `getScore - when 2 out of 4 correct decisions - returns 50`() = runBlocking {
        val score = calculator.getScore(SellRecommendationDummyAlgorithm(), setOf(newBuyStockOrder(1),
                newSellStockOrder(2), newBuyStockOrder(3), newBuyStockOrder(4)), CurrencyRateRepository)

        assertScore(50, score)
    }

    private fun assertScore(expectedValue: Int, score: ConformanceScore) {
        assertTrue("Expected score $expectedValue but was ${score.score}", score.score == expectedValue)
    }

    private fun newSellStockOrder(dateInMillis: Long): StockOrder {
        return StockOrder("Sell", "SEK", dateInMillis, StockPrice.SYMBOLS[0].first, 1.0, 0.0, 1)
    }

    private fun newBuyStockOrder(dateInMillis: Long): StockOrder {
        return StockOrder("Buy", "SEK", dateInMillis, StockPrice.SYMBOLS[0].first, 1.0, 0.0, 1)
    }

    class SellRecommendationDummyAlgorithm : RecommendationAlgorithm() {

        override suspend fun getRecommendation(
                stockPrice: StockPrice, currencyRateInSek: Double, commissionFee: Double, previousOrders: Set<StockOrder>,
        ): Recommendation {
            return Recommendation(SellStockCommand(ApplicationProvider.getApplicationContext(), 0L, Currency.getInstance("SEK"),
                    StockPrice.SYMBOLS[0].first, 0.0, 1, 0.0))
        }

    }

}
