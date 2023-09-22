package com.sundbybergsit.cromfortune.main.ui.home

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderAggregate
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.StockSplit
import com.sundbybergsit.cromfortune.main.crom.CromFortuneV1RecommendationAlgorithm
import junit.framework.TestCase.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.Currency

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class StockOrderAggregateExtensionsKtTest {

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
            recommendationAlgorithm = CromFortuneV1RecommendationAlgorithm(ApplicationProvider.getApplicationContext())
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

}

