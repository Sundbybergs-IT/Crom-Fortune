package com.sundbybergsit.cromfortune.ui.home.view

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.StockSplit
import com.sundbybergsit.cromfortune.ui.home.StockOrderAggregate
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class StockOrderAggregateTest {

    private val currency: Currency = Currency.getInstance("SEK")

    @Test
    fun `getAcquisitionValue - when buy a stock without commission fee - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            StockPrice.SYMBOLS[0].first,
            StockPrice.SYMBOLS[0].first,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                    100.099, 0.0, 1
                ), null, 0L
            )
        )

        val acquisitionValue = stockOrderAggregate.getAcquisitionValue()

        assertEquals(100.099, acquisitionValue, 0.0001)
    }

    @Test
    fun `getAcquisitionValue - when buy a stock with commission fee - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            StockPrice.SYMBOLS[0].first,
            StockPrice.SYMBOLS[0].first,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                    100.099, 10.0, 1
                ), null, 0L
            )
        )

        val acquisitionValue = stockOrderAggregate.getAcquisitionValue()

        assertEquals(110.099, acquisitionValue, 0.0001)
    }

    @Test
    fun `getAcquisitionValue - when buy a stock and sell it - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            StockPrice.SYMBOLS[0].first,
            StockPrice.SYMBOLS[0].first,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                    100.099, 10.0, 1
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                    100.099, 10.0, 1
                ), null, 1L
            )
        )

        val acquisitionValue = stockOrderAggregate.getAcquisitionValue()

        assertEquals(0.0, acquisitionValue, 0.0001)
    }

    @Test
    fun `getAcquisitionValue - when buy a stock without commission fee and then split - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            StockPrice.SYMBOLS[0].first,
            StockPrice.SYMBOLS[0].first,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                    100.0, 0.0, 1
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockSplit(
                reverse = false,
                dateInMillis = 1L,
                name = StockPrice.SYMBOLS[0].first,
                quantity = 2
            ).toStockEvent()
        )

        val acquisitionValue = stockOrderAggregate.getAcquisitionValue()

        assertEquals(50.0, acquisitionValue, 0.0001)
    }

    @Test
    fun `getQuantity - when buy a stock without commission fee and then split - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            StockPrice.SYMBOLS[0].first,
            StockPrice.SYMBOLS[0].first,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                    100.0, 0.0, 1
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockSplit(
                reverse = false,
                dateInMillis = 1L,
                name = StockPrice.SYMBOLS[0].first,
                quantity = 2
            ).toStockEvent()
        )

        val quantity = stockOrderAggregate.getQuantity()

        assertEquals(2, quantity)
    }

    @Test
    fun `getAcquisitionValue - when buy a stock without commission fee and then reverse split - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            StockPrice.SYMBOLS[0].first,
            StockPrice.SYMBOLS[0].first,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                    100.0, 0.0, 2
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockSplit(
                reverse = true,
                dateInMillis = 1L,
                name = StockPrice.SYMBOLS[0].first,
                quantity = 2
            ).toStockEvent()
        )

        val acquisitionValue = stockOrderAggregate.getAcquisitionValue()

        assertEquals(200.0, acquisitionValue, 0.0001)
    }

    @Test
    fun `getQuantity - when buy a stock without commission fee after split and then reverse split - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            StockPrice.SYMBOLS[0].first,
            StockPrice.SYMBOLS[0].first,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                    100.0, 0.0, 2
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockSplit(
                reverse = false,
                dateInMillis = 1L,
                name = StockPrice.SYMBOLS[0].first,
                quantity = 2
            ).toStockEvent()
        )
        stockOrderAggregate.aggregate(
            StockSplit(
                reverse = true,
                dateInMillis = 2L,
                name = StockPrice.SYMBOLS[0].first,
                quantity = 2
            ).toStockEvent()
        )

        val quantity = stockOrderAggregate.getQuantity()

        assertEquals(2, quantity)
    }

    @Test
    fun `getAcquisitionValue - when buy a stock without commission fee after split and then reverse split - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            StockPrice.SYMBOLS[0].first,
            StockPrice.SYMBOLS[0].first,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                    100.0, 0.0, 2
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockSplit(
                reverse = false,
                dateInMillis = 1L,
                name = StockPrice.SYMBOLS[0].first,
                quantity = 2
            ).toStockEvent()
        )
        stockOrderAggregate.aggregate(
            StockSplit(
                reverse = true,
                dateInMillis = 2L,
                name = StockPrice.SYMBOLS[0].first,
                quantity = 2
            ).toStockEvent()
        )

        val acquisitionValue = stockOrderAggregate.getAcquisitionValue()

        assertEquals(100.0, acquisitionValue, 0.0001)
    }

    @Test
    fun `getQuantity - when buy a stock without commission fee and then reverse split - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            StockPrice.SYMBOLS[0].first,
            StockPrice.SYMBOLS[0].first,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                    100.0, 0.0, 2
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockSplit(
                reverse = true,
                dateInMillis = 1L,
                name = StockPrice.SYMBOLS[0].first,
                quantity = 2
            ).toStockEvent()
        )

        val quantity = stockOrderAggregate.getQuantity()

        assertEquals(1, quantity)
    }

    @Test
    fun `getProfit - when nothing aggregated - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            StockPrice.SYMBOLS[0].first,
            StockPrice.SYMBOLS[0].first,
            currency
        )

        val profit = stockOrderAggregate.getProfit(1.0)

        assertEquals(0.0, profit, 0.000001)
    }

    @Test
    fun `getProfit - after purchase - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            StockPrice.SYMBOLS[0].first,
            StockPrice.SYMBOLS[0].first,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                    100.099, 10.0, 1
                ), null, 0L
            )
        )

        val profit = stockOrderAggregate.getProfit(0.099)

        assertEquals(-110.0, profit, 0.000001)
    }

    @Test
    fun `getProfit - after purchase and sale when nothing left - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            StockPrice.SYMBOLS[0].first,
            StockPrice.SYMBOLS[0].first,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                    100.099, 10.0, 1
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                    100.099, 10.0, 1
                ), null, 0L
            )
        )

        val profit = stockOrderAggregate.getProfit(10000000.0)

        assertEquals(0.0, profit, 0.000001)
    }

    @Test
    fun `getProfit - after purchase and sale with no commissions when stocks left - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            StockPrice.SYMBOLS[0].first,
            StockPrice.SYMBOLS[0].first,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                    100.0, 0.0, 2
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                    200.0, 0.0, 1
                ), null, 0L
            )
        )

        val profit = stockOrderAggregate.getProfit(100.0)

        assertEquals(100.0, profit, 0.000001)
    }

}
