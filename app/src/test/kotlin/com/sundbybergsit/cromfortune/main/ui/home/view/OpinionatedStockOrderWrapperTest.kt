package com.sundbybergsit.cromfortune.main.ui.home.view

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.algorithm.api.Recommendation
import com.sundbybergsit.cromfortune.algorithm.core.BuyStockCommand
import com.sundbybergsit.cromfortune.algorithm.core.SellStockCommand
import com.sundbybergsit.cromfortune.domain.StockOrder
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.*

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class OpinionatedStockOrderWrapperTest {

    @Test
    fun `isApprovedByAlgorithm - when no recommendation and buy order - returns correct value`() {
        val opinionatedStockOrderWrapper = OpinionatedStockOrderWrapper(
            StockOrder(
                "Buy", "SEK", 0L, "",
                1.0, 1.0, 1
            ), null)

        val result = opinionatedStockOrderWrapper.isApprovedByAlgorithm()

        assertFalse(result)
    }

    @Test
    fun `isApprovedByAlgorithm - when no recommendation and sell order - returns correct value`() {
        val opinionatedStockOrderWrapper = OpinionatedStockOrderWrapper(
            com.sundbybergsit.cromfortune.domain.StockOrder(
                "Sell", "SEK", 0L, "",
                1.0, 1.0, 1
            ), null)

        val result = opinionatedStockOrderWrapper.isApprovedByAlgorithm()

        assertFalse(result)
    }

    @Test
    fun `isApprovedByAlgorithm - when buy recommendation and buy order - returns correct value`() {
        val opinionatedStockOrderWrapper = OpinionatedStockOrderWrapper(
            com.sundbybergsit.cromfortune.domain.StockOrder(
                "Buy", "SEK", 0L, "",
                1.0, 1.0, 1
            ), Recommendation(
            BuyStockCommand(
                0L,
                Currency.getInstance("SEK"), "", 1.0, 1, 39.0
            )
        )
        )

        val result = opinionatedStockOrderWrapper.isApprovedByAlgorithm()

        assertTrue(result)
    }

    @Test
    fun `isApprovedByAlgorithm - when buy recommendation and sell order - returns correct value`() {
        val opinionatedStockOrderWrapper = OpinionatedStockOrderWrapper(
            com.sundbybergsit.cromfortune.domain.StockOrder(
                "Buy", "SEK", 0L, "",
                1.0, 1.0, 1
            ), Recommendation(
            SellStockCommand(
                0L,
                Currency.getInstance("SEK"), "", 1.0, 1, 39.0
            )
        )
        )

        val result = opinionatedStockOrderWrapper.isApprovedByAlgorithm()

        assertFalse(result)
    }

    @Test
    fun `isApprovedByAlgorithm - when sell recommendation and buy order - returns correct value`() {
        val opinionatedStockOrderWrapper = OpinionatedStockOrderWrapper(
            com.sundbybergsit.cromfortune.domain.StockOrder(
                "Sell", "SEK", 0L, "",
                1.0, 1.0, 1
            ), Recommendation(
            BuyStockCommand(
                0L,
                Currency.getInstance("SEK"), "", 1.0, 1, 39.0
            )
        )
        )

        val result = opinionatedStockOrderWrapper.isApprovedByAlgorithm()

        assertFalse(result)
    }

    @Test
    fun `isApprovedByAlgorithm - when sell recommendation and sell order - returns correct value`() {
        val opinionatedStockOrderWrapper = OpinionatedStockOrderWrapper(
            com.sundbybergsit.cromfortune.domain.StockOrder(
                "Sell", "SEK", 0L, "",
                1.0, 1.0, 1
            ), Recommendation(
            SellStockCommand(
                0L,
                Currency.getInstance("SEK"), "", 1.0, 1, 39.0
            )
        )
        )

        val result = opinionatedStockOrderWrapper.isApprovedByAlgorithm()

        assertTrue(result)
    }

}
