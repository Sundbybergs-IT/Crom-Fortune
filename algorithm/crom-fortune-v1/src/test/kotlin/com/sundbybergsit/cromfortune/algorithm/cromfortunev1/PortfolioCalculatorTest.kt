package com.sundbybergsit.cromfortune.algorithm.cromfortunev1

import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockSplit
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Currency

class PortfolioCalculatorTest {

    private val currency = Currency.getInstance("SEK")

    @Test
    fun `calculate - when no orders - returns empty state`() {
        val calculator = PortfolioCalculator(emptySet(), "Stock", 1.0)
        val state = calculator.calculate()
        assertEquals(0, state.grossQuantity)
        assertEquals(0, state.soldQuantity)
        assertEquals(0.0, state.accumulatedCostInSek, 0.001)
    }

    @Test
    fun `calculate - when single buy order - returns correct state`() {
        val order = StockOrder("Buy", currency.toString(), 0L, "Stock", 100.0, 10.0, 10)
        val calculator = PortfolioCalculator(setOf(order.toStockEvent()), "Stock", 1.0)
        val state = calculator.calculate()
        assertEquals(10, state.grossQuantity)
        assertEquals(0, state.soldQuantity)
        assertEquals(1010.0, state.accumulatedCostInSek, 0.001)
    }

    @Test
    fun `calculate - when buy and sell order - returns correct state`() {
        val buyOrder = StockOrder("Buy", currency.toString(), 0L, "Stock", 100.0, 10.0, 10)
        val sellOrder = StockOrder("Sell", currency.toString(), 1L, "Stock", 110.0, 10.0, 5)
        val calculator = PortfolioCalculator(setOf(buyOrder.toStockEvent(), sellOrder.toStockEvent()), "Stock", 1.0)
        val state = calculator.calculate()
        assertEquals(10, state.grossQuantity)
        assertEquals(5, state.soldQuantity)
        assertEquals(1010.0, state.accumulatedCostInSek, 0.001)
        assertEquals(5, state.netQuantity)
    }

    @Test
    fun `calculate - when stock split - returns correct state`() {
        val buyOrder = StockOrder("Buy", currency.toString(), 0L, "Stock", 100.0, 10.0, 10)
        val split = StockSplit(false, 1L, "Stock", 2)
        val calculator = PortfolioCalculator(setOf(buyOrder.toStockEvent(), split.toStockEvent()), "Stock", 1.0)
        val state = calculator.calculate()
        assertEquals(20, state.grossQuantity)
        assertEquals(0, state.soldQuantity)
        // Cost should not change? Or should it?
        // Original logic: accumulatedCostInSek += rateInSek * stockOrder.pricePerStock * stockOrder.quantity + stockOrder.commissionFee
        // It accumulates cost at the time of purchase. Split doesn't change historical cost in SEK.
        assertEquals(1010.0, state.accumulatedCostInSek, 0.001)
    }

    @Test
    fun `calculate - when reverse stock split - returns correct state`() {
        val buyOrder = StockOrder("Buy", currency.toString(), 0L, "Stock", 100.0, 10.0, 10)
        val split = StockSplit(true, 1L, "Stock", 2)
        val calculator = PortfolioCalculator(setOf(buyOrder.toStockEvent(), split.toStockEvent()), "Stock", 1.0)
        val state = calculator.calculate()
        assertEquals(5, state.grossQuantity)
        assertEquals(0, state.soldQuantity)
        assertEquals(1010.0, state.accumulatedCostInSek, 0.001)
    }
}
