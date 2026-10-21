package com.sundbybergsit.cromfortune.algorithm.cromfortunev1

import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockSplit

class PortfolioCalculator(
    private val stockEvents: Collection<StockEvent>,
    private val stockName: String,
    private val rateInSek: Double
) {

    data class PortfolioState(
        val grossQuantity: Int,
        val soldQuantity: Int,
        val accumulatedCostInSek: Double
    ) {
        val netQuantity: Int = grossQuantity - soldQuantity
    }

    fun calculate(): PortfolioState {
        val orders = stockEvents.filter { it.stockOrder != null }.map { it.stockOrder!! }
            .sortedWith(
                compareBy<StockOrder> { it.dateInMillis }
                    .thenBy { if (it.orderAction == "Buy") 0 else 1 }
            )
        val stockSplits = stockEvents.filter { it.stockSplit != null }.map { it.stockSplit!! }
            .sortedBy { it.dateInMillis }

        var grossQuantity = 0
        var soldQuantity = 0
        var accumulatedCostInSek = 0.0

        for (stockOrder in orders) {
            if (stockOrder.name == stockName) {
                val splitMultiplier = calculateSplitMultiplier(stockOrder, stockSplits)

                when (stockOrder.orderAction) {
                    "Buy" -> {
                        grossQuantity += (stockOrder.quantity.toDouble() * splitMultiplier).toInt()
                        accumulatedCostInSek += rateInSek * stockOrder.pricePerStock * stockOrder.quantity.toDouble() +
                                stockOrder.commissionFee
                    }
                    "Sell" -> {
                        soldQuantity += (stockOrder.quantity.toDouble() * splitMultiplier).toInt()
                    }
                    "Dividend" -> {
                        // Dividends do not alter stock quantity or cost basis
                    }
                }

                if (grossQuantity - soldQuantity <= 0) {
                    accumulatedCostInSek = 0.0
                    grossQuantity = 0
                    soldQuantity = 0
                }
            }
        }
        return PortfolioState(grossQuantity, soldQuantity, accumulatedCostInSek)
    }

    private fun calculateSplitMultiplier(stockOrder: StockOrder, sortedSplits: List<StockSplit>): Double {
        var splitMultiplier = 1.0
        for (sortedSplit in sortedSplits) {
            if (sortedSplit.dateInMillis > stockOrder.dateInMillis) {
                if (sortedSplit.reverse) {
                    splitMultiplier /= sortedSplit.quantity.toDouble()
                } else {
                    splitMultiplier *= sortedSplit.quantity.toDouble()
                }
            }
        }
        return splitMultiplier
    }
}
