package com.sundbybergsit.cromfortune.algorithm.cromfortunev1

import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockSplit
import java.util.SortedSet

class PortfolioCalculator(
    private val stockEvents: Set<StockEvent>,
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
            .toSortedSet { s1, s2 -> s1.dateInMillis.compareTo(s2.dateInMillis) }
        val stockSplits = stockEvents.filter { it.stockSplit != null }.map { it.stockSplit!! }
            .toSortedSet { s1, s2 -> s1.dateInMillis.compareTo(s2.dateInMillis) }

        var grossQuantity = 0
        var soldQuantity = 0
        var accumulatedCostInSek = 0.0

        for (stockOrder in orders) {
            if (stockOrder.name == stockName) {
                val splitMultiplier = calculateSplitMultiplier(stockOrder, stockSplits)

                if (stockOrder.orderAction == "Buy") {
                    grossQuantity += (stockOrder.quantity * splitMultiplier).toInt()
                    accumulatedCostInSek += rateInSek * stockOrder.pricePerStock * stockOrder.quantity +
                            stockOrder.commissionFee
                } else {
                    soldQuantity += (stockOrder.quantity * splitMultiplier).toInt()
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

    private fun calculateSplitMultiplier(stockOrder: StockOrder, sortedSplits: SortedSet<StockSplit>): Double {
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
