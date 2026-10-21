package com.sundbybergsit.cromfortune.domain

import org.junit.jupiter.api.Test
import java.util.Currency
import kotlin.test.assertEquals

class StockOrderAggregateTest {

    private val currency: Currency = Currency.getInstance("SEK")

    @Test
    fun `getAcquisitionValue - when buy a stock without commission fee - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            AssetCatalog.stocks[0].symbol,
            AssetCatalog.stocks[0].symbol,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, AssetCatalog.stocks[0].symbol,
                    100.099, 0.0, 1
                ), null, 0L
            )
        )

        val acquisitionValue = stockOrderAggregate.getAcquisitionValue()

        assertEquals(expected = 100.099, actual = acquisitionValue, absoluteTolerance = 0.0001)
    }

    @Test
    fun `getAcquisitionValue - when buy a stock with commission fee - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            AssetCatalog.stocks[0].symbol,
            AssetCatalog.stocks[0].symbol,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, AssetCatalog.stocks[0].symbol,
                    100.099, 10.0, 1
                ), null, 0L
            )
        )

        val acquisitionValue = stockOrderAggregate.getAcquisitionValue()

        assertEquals(expected = 110.099, actual = acquisitionValue, absoluteTolerance = 0.0001)
    }

    @Test
    fun `getAcquisitionValue - when buy a stock and sell it - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            AssetCatalog.stocks[0].symbol,
            AssetCatalog.stocks[0].symbol,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, AssetCatalog.stocks[0].symbol,
                    100.099, 10.0, 1
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), 0L, AssetCatalog.stocks[0].symbol,
                    100.099, 10.0, 1
                ), null, 1L
            )
        )

        val acquisitionValue = stockOrderAggregate.getAcquisitionValue()

        assertEquals(expected = 0.0, actual = acquisitionValue, absoluteTolerance = 0.0001)
    }

    @Test
    fun `getAcquisitionValue - when buy a stock without commission fee and then split - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            AssetCatalog.stocks[0].symbol,
            AssetCatalog.stocks[0].symbol,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, AssetCatalog.stocks[0].symbol,
                    100.0, 0.0, 1
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockSplit(
                reverse = false,
                dateInMillis = 1L,
                name = AssetCatalog.stocks[0].symbol,
                quantity = 2
            ).toStockEvent()
        )

        val acquisitionValue = stockOrderAggregate.getAcquisitionValue()

        assertEquals(expected = 50.0, actual = acquisitionValue, absoluteTolerance = 0.0001)
    }

    @Test
    fun `getAcquisitionValue - issue 45 - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            AssetCatalog.stocks[0].symbol,
            AssetCatalog.stocks[0].symbol,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, AssetCatalog.stocks[0].symbol,
                    17.10, 38.0, 200
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 1L, AssetCatalog.stocks[0].symbol,
                    15.90, 38.0, 150
                ), null, 1L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 2L, AssetCatalog.stocks[0].symbol,
                    12.50, 0.0, 14
                ), null, 2L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), 3L, AssetCatalog.stocks[0].symbol,
                    17.66, 13.0, 277
                ), null, 3L
            )
        )

        val acquisitionValue = stockOrderAggregate.getAcquisitionValue()
        val realizedProfit = stockOrderAggregate.getRealizedProfit()

        assertEquals(expected = 16.63736, actual = acquisitionValue, absoluteTolerance = 0.0001)
        assertEquals(expected = 270.27055, actual = realizedProfit, absoluteTolerance = 0.0001)
    }

    @Test
    fun `getAcquisitionValue - issue 52 - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            AssetCatalog.stocks[0].symbol,
            AssetCatalog.stocks[0].symbol,
            currency
        )
        var dateInMillis = 0L
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), dateInMillis, AssetCatalog.stocks[0].symbol,
                    22.50, 38.0, 150
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), dateInMillis, AssetCatalog.stocks[0].symbol,
                    20.50, 38.0, 150
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), dateInMillis, AssetCatalog.stocks[0].symbol,
                    18.38, 38.0, 100
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), dateInMillis, AssetCatalog.stocks[0].symbol,
                    20.10, 38.0, 150
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), dateInMillis, AssetCatalog.stocks[0].symbol,
                    19.74, 38.0, 200
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), dateInMillis, AssetCatalog.stocks[0].symbol,
                    19.12, 38.0, 150
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), dateInMillis, AssetCatalog.stocks[0].symbol,
                    18.48, 38.0, 100
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), dateInMillis, AssetCatalog.stocks[0].symbol,
                    15.52, 38.0, 9
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), dateInMillis, AssetCatalog.stocks[0].symbol,
                    15.16, 38.0, 200
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), dateInMillis, AssetCatalog.stocks[0].symbol,
                    20.70, 38.0, 409
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), dateInMillis, AssetCatalog.stocks[0].symbol,
                    17.85, 38.0, 120
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), dateInMillis, AssetCatalog.stocks[0].symbol,
                    22.30, 38.0, 200
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), dateInMillis, AssetCatalog.stocks[0].symbol,
                    22.64, 38.0, 300
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), dateInMillis, AssetCatalog.stocks[0].symbol,
                    22.86, 38.0, 50
                ), null, dateInMillis++
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), dateInMillis, AssetCatalog.stocks[0].symbol,
                    22.94, 38.0, 100
                ), null, dateInMillis
            )
        )

        val acquisitionValue = stockOrderAggregate.getAcquisitionValue()

        assertEquals(expected = 19.25788, actual = acquisitionValue, absoluteTolerance = 0.0001)
    }

    @Test
    fun `user scenario - buy, sell for profit, buy again - calculates correct GAV and realized profit`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            AssetCatalog.stocks[0].symbol,
            AssetCatalog.stocks[0].symbol,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder("Buy", currency.toString(), 0L, AssetCatalog.stocks[0].symbol, 5.0, 0.0, 10),
                null,
                0L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder("Sell", currency.toString(), 1L, AssetCatalog.stocks[0].symbol, 10.0, 0.0, 10),
                null,
                1L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder("Buy", currency.toString(), 2L, AssetCatalog.stocks[0].symbol, 7.0, 0.0, 5),
                null,
                2L
            )
        )

        val acquisitionValue = stockOrderAggregate.getAcquisitionValue()
        val realizedProfit = stockOrderAggregate.getRealizedProfit()

        assertEquals(expected = 5.0, actual = stockOrderAggregate.getExactQuantity().toDouble(), absoluteTolerance = 0.0001)
        assertEquals(expected = 7.0, actual = acquisitionValue, absoluteTolerance = 0.0001)
        assertEquals(expected = 50.0, actual = realizedProfit, absoluteTolerance = 0.0001)
    }

    @Test
    fun `getExactQuantity - when buy a stock without commission fee and then split - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            AssetCatalog.stocks[0].symbol,
            AssetCatalog.stocks[0].symbol,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, AssetCatalog.stocks[0].symbol,
                    100.0, 0.0, 1
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockSplit(
                reverse = false,
                dateInMillis = 1L,
                name = AssetCatalog.stocks[0].symbol,
                quantity = 2
            ).toStockEvent()
        )

        val quantity = stockOrderAggregate.getExactQuantity()

        assertEquals(expected = 2.toBigDecimal(), actual = quantity)
    }

    @Test
    fun `getAcquisitionValue - when buy a stock without commission fee and then reverse split - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            AssetCatalog.stocks[0].symbol,
            AssetCatalog.stocks[0].symbol,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, AssetCatalog.stocks[0].symbol,
                    100.0, 0.0, 2
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockSplit(
                reverse = true,
                dateInMillis = 1L,
                name = AssetCatalog.stocks[0].symbol,
                quantity = 2
            ).toStockEvent()
        )

        val acquisitionValue = stockOrderAggregate.getAcquisitionValue()

        assertEquals(expected = 200.0, actual = acquisitionValue, absoluteTolerance = 0.0001)
    }

    @Test
    fun `getExactQuantity - when buy a stock without commission fee after split and then reverse split - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            AssetCatalog.stocks[0].symbol,
            AssetCatalog.stocks[0].symbol,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, AssetCatalog.stocks[0].symbol,
                    100.0, 0.0, 2
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockSplit(
                reverse = false,
                dateInMillis = 1L,
                name = AssetCatalog.stocks[0].symbol,
                quantity = 2
            ).toStockEvent()
        )
        stockOrderAggregate.aggregate(
            StockSplit(
                reverse = true,
                dateInMillis = 2L,
                name = AssetCatalog.stocks[0].symbol,
                quantity = 2
            ).toStockEvent()
        )

        val quantity = stockOrderAggregate.getExactQuantity()

        assertEquals(expected = 2.toBigDecimal(), actual = quantity)
    }

    @Test
    fun `getAcquisitionValue - when buy a stock without commission fee after split and then reverse split - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            AssetCatalog.stocks[0].symbol,
            AssetCatalog.stocks[0].symbol,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, AssetCatalog.stocks[0].symbol,
                    100.0, 0.0, 2
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockSplit(
                reverse = false,
                dateInMillis = 1L,
                name = AssetCatalog.stocks[0].symbol,
                quantity = 2
            ).toStockEvent()
        )
        stockOrderAggregate.aggregate(
            StockSplit(
                reverse = true,
                dateInMillis = 2L,
                name = AssetCatalog.stocks[0].symbol,
                quantity = 2
            ).toStockEvent()
        )

        val acquisitionValue = stockOrderAggregate.getAcquisitionValue()

        assertEquals(expected = 100.0, actual = acquisitionValue, absoluteTolerance = 0.0001)
    }

    @Test
    fun `getExactQuantity - when buy a stock without commission fee and then reverse split - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            AssetCatalog.stocks[0].symbol,
            AssetCatalog.stocks[0].symbol,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, AssetCatalog.stocks[0].symbol,
                    100.0, 0.0, 2
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockSplit(
                reverse = true,
                dateInMillis = 1L,
                name = AssetCatalog.stocks[0].symbol,
                quantity = 2
            ).toStockEvent()
        )

        val quantity = stockOrderAggregate.getExactQuantity()

        assertEquals(expected = 1.toBigDecimal(), actual = quantity)
    }

    @Test
    fun `getProfit - when nothing aggregated - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            AssetCatalog.stocks[0].symbol,
            AssetCatalog.stocks[0].symbol,
            currency
        )

        val profit = stockOrderAggregate.getProfit(1.0)

        assertEquals(expected = 0.0, actual = profit, absoluteTolerance = 0.000001)
    }

    @Test
    fun `getProfit - after purchase - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            AssetCatalog.stocks[0].symbol,
            AssetCatalog.stocks[0].symbol,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, AssetCatalog.stocks[0].symbol,
                    100.099, 10.0, 1
                ), null, 0L
            )
        )

        val profit = stockOrderAggregate.getProfit(0.099)

        assertEquals(expected = -110.0, actual = profit, 0.000001)
    }

    @Test
    fun `getProfit - after purchase and sale when nothing left - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            AssetCatalog.stocks[0].symbol,
            AssetCatalog.stocks[0].symbol,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, AssetCatalog.stocks[0].symbol,
                    100.099, 10.0, 1
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), 0L, AssetCatalog.stocks[0].symbol,
                    100.099, 10.0, 1
                ), null, 0L
            )
        )

        val profit = stockOrderAggregate.getProfit(10000000.0)

        assertEquals(expected = -20.0, actual = profit, absoluteTolerance = 0.000001)
    }

    @Test
    fun `getProfit - after purchase and sale with no commissions when stocks left - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            AssetCatalog.stocks[0].symbol,
            AssetCatalog.stocks[0].symbol,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, AssetCatalog.stocks[0].symbol,
                    100.0, 0.0, 2
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), 0L, AssetCatalog.stocks[0].symbol,
                    200.0, 0.0, 1
                ), null, 0L
            )
        )

        val profit = stockOrderAggregate.getProfit(100.0)

        assertEquals(expected = 100.0, actual = profit, absoluteTolerance = 0.000001)
    }

    @Test
    fun `getProfit - bug 48 sample - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(
            1.0,
            AssetCatalog.stocks[0].symbol,
            AssetCatalog.stocks[0].symbol,
            currency
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 0L, AssetCatalog.stocks[0].symbol,
                    35.30, 5.0, 100
                ), null, 0L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), 1L, AssetCatalog.stocks[0].symbol,
                    46.70, 5.0, 20
                ), null, 1L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), 2L, AssetCatalog.stocks[0].symbol,
                    50.44, 5.0, 19
                ), null, 2L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Sell", currency.toString(), 3L, AssetCatalog.stocks[0].symbol,
                    53.92, 5.0, 18
                ), null, 3L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 4L, AssetCatalog.stocks[0].symbol,
                    52.58, 5.0, 190
                ), null, 4L
            )
        )
        stockOrderAggregate.aggregate(
            StockEvent(
                StockOrder(
                    "Buy", currency.toString(), 5L, AssetCatalog.stocks[0].symbol,
                    47.87, 5.0, 40
                ), null, 5L
            )
        )

        val profit = stockOrderAggregate.getProfit(53.90)

        assertEquals(expected = 2112.62, actual = profit, absoluteTolerance = 0.000001)
    }

}
