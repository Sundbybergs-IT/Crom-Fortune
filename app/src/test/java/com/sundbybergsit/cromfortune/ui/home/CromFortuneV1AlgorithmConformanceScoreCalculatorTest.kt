package com.sundbybergsit.cromfortune.ui.home

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@Config(sdk = [Build.VERSION_CODES.Q])
@RunWith(AndroidJUnit4::class)
class CromFortuneV1AlgorithmConformanceScoreCalculatorTest {

    private lateinit var calculator: CromFortuneV1AlgorithmConformanceScoreCalculator

    @Before
    fun setUp() {
        calculator = CromFortuneV1AlgorithmConformanceScoreCalculator()
    }

    @Test
    fun `getScore - when no orders - returns 100`() {
        runBlocking {
            val score = calculator.getScore(SellRecommendationDummyAlgorithm(), emptySet(), CurrencyRateRepository)

            assertTrue(score.score == 100)
        }
    }

    @Test(expected = IllegalStateException::class)
    fun `getScore - when initial sell order - throws exception`() {
        runBlocking {
            calculator.getScore(SellRecommendationDummyAlgorithm(), setOf(newSellStockOrder(1)), CurrencyRateRepository)
        }
    }

    @Test
    fun `getScore - when initial buy order - returns 100`() {
        runBlocking {
            val score = calculator.getScore(SellRecommendationDummyAlgorithm(), setOf(newBuyStockOrder(1)), CurrencyRateRepository)

            assertTrue(score.score == 100)
        }
    }

    @Test
    fun `getScore - when 0 out of 1 correct decisions - returns 0`() {
        runBlocking {
            val score = calculator.getScore(SellRecommendationDummyAlgorithm(), setOf(newBuyStockOrder(1),
                    newBuyStockOrder(2)), CurrencyRateRepository)

            assertTrue(score.score == 0)
        }
    }

    @Test
    fun `getScore - when 1 out of 1 correct decisions - returns 100`() {
        runBlocking {
            val score = calculator.getScore(SellRecommendationDummyAlgorithm(), setOf(newBuyStockOrder(1),
                    newSellStockOrder(2)), CurrencyRateRepository)

            assertTrue(score.score == 100)
        }
    }

    @Test
    fun `getScore - when 1 out of 2 correct decisions - returns 50`() {
        runBlocking {
            val score = calculator.getScore(SellRecommendationDummyAlgorithm(), setOf(newBuyStockOrder(1),
                    newSellStockOrder(2), newBuyStockOrder(3)), CurrencyRateRepository)

            assertTrue(score.score == 50)
        }
    }

    private fun newSellStockOrder(dateInMillis: Long): StockOrder {
        return StockOrder("Sell", "SEK", dateInMillis, "", 1.0, 0.0, 1)
    }

    private fun newBuyStockOrder(dateInMillis: Long): StockOrder {
        return StockOrder("Buy", "SEK", dateInMillis, "", 1.0, 0.0, 1)
    }

    class SellRecommendationDummyAlgorithm : RecommendationAlgorithm() {

        override suspend fun getRecommendation(
                stockPrice: StockPrice, currencyRateInSek : Double, commissionFee: Double, previousOrders: Set<StockOrder>,
        ): Recommendation {
            return Recommendation(SellStockCommand(ApplicationProvider.getApplicationContext(), 0L, Currency.getInstance("SEK"),
                    "", 0.0, 1, 0.0))
        }

    }

}
