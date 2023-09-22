package com.sundbybergsit.cromfortune.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Currency

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
    fun `getAcquisitionValue - issue 45 - returns correct value`() {
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
                    17.10, 38.0, 200
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 1L, StockPrice.SYMBOLS[0].first,
                    15.90, 38.0, 150
                ), null, 1L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 2L, StockPrice.SYMBOLS[0].first,
                    12.50, 0.0, 14
                ), null, 2L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), 3L, StockPrice.SYMBOLS[0].first,
                    17.66, 13.0, 277
                ), null, 3L
            )
        )

        val acquisitionValue = stockOrderAggregate.getAcquisitionValue()

        assertEquals(13.53080, acquisitionValue, 0.0001)
    }

    @Test
    fun `getAcquisitionValue - issue 52 - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            StockPrice.SYMBOLS[0].first,
            StockPrice.SYMBOLS[0].first,
            currency
        )
        var dateInMillis = 0L
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), dateInMillis, StockPrice.SYMBOLS[0].first,
                    22.50, 38.0, 150
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), dateInMillis, StockPrice.SYMBOLS[0].first,
                    20.50, 38.0, 150
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), dateInMillis, StockPrice.SYMBOLS[0].first,
                    18.38, 38.0, 100
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), dateInMillis, StockPrice.SYMBOLS[0].first,
                    20.10, 38.0, 150
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), dateInMillis, StockPrice.SYMBOLS[0].first,
                    19.74, 38.0, 200
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), dateInMillis, StockPrice.SYMBOLS[0].first,
                    19.12, 38.0, 150
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), dateInMillis, StockPrice.SYMBOLS[0].first,
                    18.48, 38.0, 100
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), dateInMillis, StockPrice.SYMBOLS[0].first,
                    15.52, 38.0, 9
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), dateInMillis, StockPrice.SYMBOLS[0].first,
                    15.16, 38.0, 200
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), dateInMillis, StockPrice.SYMBOLS[0].first,
                    20.70, 38.0, 409
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), dateInMillis, StockPrice.SYMBOLS[0].first,
                    17.85, 38.0, 120
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), dateInMillis, StockPrice.SYMBOLS[0].first,
                    22.30, 38.0, 200
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), dateInMillis, StockPrice.SYMBOLS[0].first,
                    22.64, 38.0, 300
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), dateInMillis, StockPrice.SYMBOLS[0].first,
                    22.86, 38.0, 50
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), dateInMillis, StockPrice.SYMBOLS[0].first,
                    22.94, 38.0, 100
                ), null, dateInMillis
            )
        )

        val acquisitionValue = stockOrderAggregate.getAcquisitionValue()

        assertEquals(9.98288888, acquisitionValue, 0.0001)
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

        assertEquals(-20.0, profit, 0.000001)
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

    @Test
    fun `getProfit - bug 48 sample - returns correct value`() {
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
                    35.30, 5.0, 100
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), 1L, StockPrice.SYMBOLS[0].first,
                    46.70, 5.0, 20
                ), null, 1L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), 2L, StockPrice.SYMBOLS[0].first,
                    50.44, 5.0, 19
                ), null, 2L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), 3L, StockPrice.SYMBOLS[0].first,
                    53.92, 5.0, 18
                ), null, 3L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 4L, StockPrice.SYMBOLS[0].first,
                    52.58, 5.0, 190
                ), null, 4L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 5L, StockPrice.SYMBOLS[0].first,
                    47.87, 5.0, 40
                ), null, 5L
            )
        )

        val profit = stockOrderAggregate.getProfit(53.90)

        assertEquals(2112.62, profit, 0.000001)
    }

}
