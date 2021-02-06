package com.sundbybergsit.cromfortune.ui.home

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@Config(sdk = [Build.VERSION_CODES.Q])
@RunWith(AndroidJUnit4::class)
class StockOrderAggregateTest {

    private val currency: Currency = Currency.getInstance("SEK")

    @Test
    fun `getAcquisitionValue - when buy a stock without commission fee - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(1.0, StockPrice.SYMBOLS[0].first, StockPrice.SYMBOLS[0].first,
                currency)
        stockOrderAggregate.aggregate(StockOrder("Buy", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                100.099, 0.0, 1))

        val acquisitionValue = stockOrderAggregate.getAcquisitionValue()

        assertEquals(100.099, acquisitionValue, 0.0001)
    }

    @Test
    fun `getAcquisitionValue - when buy a stock with commission fee - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(1.0, StockPrice.SYMBOLS[0].first, StockPrice.SYMBOLS[0].first,
                currency)
        stockOrderAggregate.aggregate(StockOrder("Buy", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                100.099, 10.0, 1))

        val acquisitionValue = stockOrderAggregate.getAcquisitionValue()

        assertEquals(110.099, acquisitionValue, 0.0001)
    }

    @Test
    fun `getAcquisitionValue - when buy a stock and sell it - returns correct value`() {
        val stockOrderAggregate = StockOrderAggregate(1.0, StockPrice.SYMBOLS[0].first, StockPrice.SYMBOLS[0].first,
                currency)
        stockOrderAggregate.aggregate(StockOrder("Buy", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                100.099, 10.0, 1))
        stockOrderAggregate.aggregate(StockOrder("Sell", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                100.099, 10.0, 1))

        val acquisitionValue = stockOrderAggregate.getAcquisitionValue()

        assertEquals(110.099, acquisitionValue, 0.0001)
    }

}
