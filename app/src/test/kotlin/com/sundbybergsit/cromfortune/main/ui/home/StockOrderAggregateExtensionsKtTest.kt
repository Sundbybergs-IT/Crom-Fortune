package com.sundbybergsit.cromfortune.main.ui.home

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.algorithm.api.Recommendation
import com.sundbybergsit.cromfortune.algorithm.api.RecommendationAlgorithm
import com.sundbybergsit.cromfortune.algorithm.core.BuyStockCommand
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderAggregate
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.StockSplit
import com.sundbybergsit.cromfortune.domain.notifications.NotificationMessage
import com.sundbybergsit.cromfortune.main.crom.CromFortuneV1RecommendationAlgorithm
import junit.framework.TestCase.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.math.BigDecimal
import java.util.Currency
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class StockOrderAggregateExtensionsKtTest {

    @Test
    fun `notification decision cannot add cash to Crom wallet`() {
        val aggregate = aggregateWithOneShare()
        val notification = notification(price = 80.0)

        val event = aggregate.applyNotificationForRecommendedEvent(
            notification = notification,
            existingEvents = aggregate.events,
            recommendationAlgorithm = AlwaysBuyAlgorithm(),
            cromCashWallet = SimulatedCashWallet()
        )

        assertNull(event)
    }

    @Test
    fun `notification decision buys using existing Crom cash`() {
        val aggregate = aggregateWithOneShare()
        val notification = notification(price = 80.0)
        val wallet = SimulatedCashWallet(BigDecimal("200"))

        val event = aggregate.applyNotificationForRecommendedEvent(
            notification = notification,
            existingEvents = aggregate.events,
            recommendationAlgorithm = AlwaysBuyAlgorithm(),
            cromCashWallet = wallet
        )

        assertNotNull(event)
        assertEquals("Buy", event.stockOrder?.orderAction)
        assertEquals(0, BigDecimal("81").compareTo(wallet.creditSek))
    }

    @Test
    fun `applyStockOrderForRecommendedEvent - after reverse split - returns no new event`() {
        val ticker = StockPrice.SYMBOLS[0].first
        val stockOrderAggregate = StockOrderAggregate(
            rateInSek = 1.0,
            displayName = "",
            stockSymbol = ticker,
            currency = Currency.getInstance(StockPrice.SYMBOLS[0].third)
        )
        val stockEvent1 = newBuyStockEvent(
            dateInMillis = 0L,
            ticker = ticker,
            price = 0.80,
            quantity = 1500
        )
        val stockEvent2 = StockSplit(
            reverse = true,
            dateInMillis = 20000L,
            name = ticker,
            quantity = 8
        ).toStockEvent()
        stockOrderAggregate.aggregate(stockEvent1)
        stockOrderAggregate.aggregate(stockEvent2)
        val newStockEvent = stockOrderAggregate.applyStockOrderForRecommendedEvent(
            eventToConsider = newSellStockEvent(
                dateInMillis = 300000L,
                ticker = ticker,
                price = 2.20
            ),
            existingEvents = listOf(stockEvent1, stockEvent2),
            recommendationAlgorithm = CromFortuneV1RecommendationAlgorithm(),
            cromCashWallet = SimulatedCashWallet(),
            userSimulatedCashWallet = SimulatedCashWallet()
        )

        assertNull(newStockEvent)
    }

    private fun newSellStockEvent(
        dateInMillis: Long,
        ticker: String = StockPrice.SYMBOLS[0].first,
        price: Double = 1.0
    ): StockEvent {
        return StockOrder(
            "Sell",
            "SEK",
            dateInMillis,
            ticker,
            price,
            0.0,
            1
        ).toStockEvent()
    }

    private fun newBuyStockEvent(
        dateInMillis: Long,
        ticker: String = StockPrice.SYMBOLS[0].first,
        price: Double = 1.0,
        quantity: Int = 1,
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

    private fun aggregateWithOneShare(): StockOrderAggregate {
        val ticker = StockPrice.SYMBOLS[0].first
        return StockOrderAggregate(1.0, ticker, ticker, Currency.getInstance("SEK")).also {
            it.aggregate(newBuyStockEvent(0L, ticker, price = 100.0))
        }
    }

    private fun notification(price: Double) = NotificationMessage(
        dateInMillis = 10L,
        message = "recommendation",
        portfolioName = "Default",
        stockSymbol = StockPrice.SYMBOLS[0].first,
        currencyCode = "SEK",
        pricePerStock = price
    )

    private class AlwaysBuyAlgorithm : RecommendationAlgorithm() {
        override val supportedAssetTypes = setOf(AssetType.STOCK)

        override fun getRecommendation(
            stockPrice: StockPrice,
            currencyRateInSek: Double,
            commissionFee: Double,
            stockEvents: Set<StockEvent>,
            timeInMillis: Long
        ) = Recommendation(
            BuyStockCommand(timeInMillis, stockPrice.currency, stockPrice.stockSymbol, stockPrice.price, 1, commissionFee)
        )
    }

}

