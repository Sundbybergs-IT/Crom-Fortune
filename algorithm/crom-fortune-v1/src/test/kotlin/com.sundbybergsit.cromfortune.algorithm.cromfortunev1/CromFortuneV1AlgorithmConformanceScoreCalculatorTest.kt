package com.sundbybergsit.cromfortune.algorithm.cromfortunev1

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.algorithm.api.Recommendation
import com.sundbybergsit.cromfortune.algorithm.api.RecommendationAlgorithm
import com.sundbybergsit.cromfortune.algorithm.core.ConformanceScore
import com.sundbybergsit.cromfortune.algorithm.core.SellStockCommand
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.StockSplit
import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.util.Currency

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class CromFortuneV1AlgorithmConformanceScoreCalculatorTest {

    private lateinit var calculator: CromFortuneV1AlgorithmConformanceScoreCalculator

    private val currencyRateApi = StubbedCurrencyRateApi()
    private val stockPriceApi = StubbedStockPriceApi()

    @get:Rule
    val coroutineScopeTestRule = CoroutineScopeTestRule()

    @Before
    fun setUp() {
        currencyRateApi.addAll(setOf(CurrencyRate("SEK", 1.0)))
        stockPriceApi.put(
            setOf(
                StockPrice(StockPrice.SYMBOLS[0].first, Currency.getInstance("SEK"), 1.0)
            )
        )
        ShadowLooper.runUiThreadTasks()
        calculator = CromFortuneV1AlgorithmConformanceScoreCalculator()
    }

    @Test
    fun `getScore - when no orders - returns 100`() = runBlocking {
        val score = calculator.getScore(SellRecommendationDummyAlgorithm(), emptySet(), currencyRateApi)

        assertScore(100, score)
    }

    @Test(expected = IllegalStateException::class)
    fun `getScore - when initial sell order - throws exception`() {
        runBlocking {
            calculator.getScore(
                SellRecommendationDummyAlgorithm(),
                setOf(newSellStockEvent(1)),
                currencyRateApi
            )
        }
    }

    @Test
    fun `getScore - when initial split order - returns 100`() = runBlocking {
        val score = calculator.getScore(
            SellRecommendationDummyAlgorithm(),
            setOf(StockSplit(false, 1L, StockPrice.SYMBOLS[0].first, 2).toStockEvent()),
            currencyRateApi
        )

        assertScore(100, score)
    }

    @Test
    fun `getScore - when initial buy order - returns 100`() = runBlocking {
        val score =
            calculator.getScore(
                SellRecommendationDummyAlgorithm(),
                setOf(newBuyStockEvent(1)),
                currencyRateApi
            )

        assertScore(100, score)
    }

    @Test
    fun `getScore - when 1 out of 2 correct decisions - returns 50`() = runBlocking {
        val score = calculator.getScore(
            SellRecommendationDummyAlgorithm(),
            setOf(
                newBuyStockEvent(1),
                newBuyStockEvent(2)
            ),
            currencyRateApi
        )

        assertScore(50, score)
    }

    @Test
    fun `getScore - when 1 out of 2 correct decisions with splits that should not affect - returns 50`() = runBlocking {
        val ticker = StockPrice.SYMBOLS[0].first
        val score = calculator.getScore(
            SellRecommendationDummyAlgorithm(),
            setOf(
                StockSplit(true, 2L, ticker, 5).toStockEvent(),
                newBuyStockEvent(1, ticker),
                StockSplit(false, 2L, ticker, 5).toStockEvent(),
                newBuyStockEvent(2, ticker)
            ),
            currencyRateApi
        )

        assertScore(50, score)
    }

    @Test
    fun `getScore - when 2 out of 2 correct decisions with split that should affect - returns 100`() = runBlocking {
        val ticker = StockPrice.SYMBOLS[0].first
        val score = calculator.getScore(
            CromFortuneV1RecommendationAlgorithm(context = ApplicationProvider.getApplicationContext()),
            setOf(
                newBuyStockEvent(dateInMillis = 1, ticker = ticker, price = 5.0),
                StockSplit(false, 2L, ticker, 1000).toStockEvent(),
                newSellStockEvent(dateInMillis = 300000000L, ticker = ticker, price = 5.0)
            ),
            currencyRateApi
        )

        assertScore(100, score)
    }

    @Test
    fun `getScore - when 1 out of 2 correct decisions with split that should affect - returns 50`() = runBlocking {
        val ticker = StockPrice.SYMBOLS[0].first
        val score = calculator.getScore(
            CromFortuneV1RecommendationAlgorithm(context = ApplicationProvider.getApplicationContext()),
            setOf(
                newBuyStockEvent(dateInMillis = 1, ticker = ticker, price = 5.0, quantity = 1000),
                StockSplit(true, 2L, ticker, 1000).toStockEvent(),
                newSellStockEvent(dateInMillis = 300000000L, ticker = ticker, price = 5.0, quantity = 1)
            ),
            currencyRateApi
        )

        assertScore(50, score)
    }

    @Test
    fun `getScore - when 2 out of 2 correct decisions - returns 100`() = runBlocking {
        val score = calculator.getScore(
            SellRecommendationDummyAlgorithm(),
            setOf(
                newBuyStockEvent(1),
                newSellStockEvent(2)
            ),
            currencyRateApi
        )

        assertScore(100, score)
    }

    @Test
    fun `getScore - when 2 out of 3 correct decisions - returns 66`() = runBlocking {
        val score = calculator.getScore(
            SellRecommendationDummyAlgorithm(),
            setOf(
                newBuyStockEvent(1),
                newSellStockEvent(2),
                newBuyStockEvent(3)
            ),
            currencyRateApi
        )

        assertScore(66, score)
    }

    @Test
    fun `getScore - when 2 out of 4 correct decisions - returns 50`() = runBlocking {
        val score = calculator.getScore(
            SellRecommendationDummyAlgorithm(),
            setOf(
                newBuyStockEvent(1),
                newSellStockEvent(2),
                newBuyStockEvent(3),
                newBuyStockEvent(4)
            ),
            currencyRateApi
        )

        assertScore(50, score)
    }

    private fun assertScore(expectedValue: Int, score: ConformanceScore) {
        assertTrue("Expected score $expectedValue but was ${score.score}", score.score == expectedValue)
    }

    private fun newSellStockEvent(
        dateInMillis: Long,
        ticker: String = StockPrice.SYMBOLS[0].first,
        price: Double = 1.0,
        quantity: Int = 1
    ): StockEvent {
        return StockOrder(
            "Sell",
            "SEK",
            dateInMillis,
            ticker,
            price,
            0.0,
            quantity
        ).toStockEvent()
    }

    private fun newBuyStockEvent(
        dateInMillis: Long,
        ticker: String = StockPrice.SYMBOLS[0].first,
        price: Double = 1.0,
        quantity: Int = 1
    ): StockEvent {
        return StockOrder(
            "Buy",
            "SEK",
            dateInMillis,
            ticker,
            price,
            0.0,
            quantity
        ).toStockEvent()
    }

    class SellRecommendationDummyAlgorithm : RecommendationAlgorithm() {

        override fun getRecommendation(
            stockPrice: StockPrice, currencyRateInSek: Double, commissionFee: Double, stockEvents: Set<StockEvent>,
            timeInMillis: Long,
        ): Recommendation {
            return Recommendation(
                SellStockCommand(
                    timeInMillis, Currency.getInstance("SEK"),
                    StockPrice.SYMBOLS[0].first,
                    0.0, 1, 0.0
                )
            )
        }

    }

}
