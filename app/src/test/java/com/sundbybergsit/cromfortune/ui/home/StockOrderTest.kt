package com.sundbybergsit.cromfortune.ui.home

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.currencies.CurrencyRate
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import java.util.*

@Config(sdk = [Build.VERSION_CODES.Q])
@RunWith(AndroidJUnit4::class)
class StockOrderTest {

    private val currency: Currency = Currency.getInstance("SEK")

    @Before
    fun setUp() {
        CurrencyRateRepository.add(setOf(CurrencyRate("SEK", 1.0)))
        ShadowLooper.runUiThreadTasks()
    }

    @Test
    fun `getAcquisitionValue - when buy a stock without commission fee - returns correct value`() {
        val acquisitionValue = StockOrder("Buy", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                100.099, 0.0, 1).getAcquisitionValue()

        assertEquals(100.099, acquisitionValue, 0.0001)
    }

    @Test
    fun `getAcquisitionValue - when buy a stock with commission fee - returns correct value`() {
        val acquisitionValue = StockOrder("Buy", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                100.099, 10.0, 1).getAcquisitionValue()

        assertEquals(110.099, acquisitionValue, 0.0001)
    }

    @Test
    fun `getAcquisitionValue - when sell a stock - returns correct value`() {
        val acquisitionValue = StockOrder("Sell", currency.toString(), 0L, StockPrice.SYMBOLS[0].first,
                100.099, 10.0, 1).getAcquisitionValue()

        assertEquals(0.0, acquisitionValue, 0.0001)
    }

}
