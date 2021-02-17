package com.sundbybergsit.cromfortune.ui.home

import com.sundbybergsit.cromfortune.stocks.StockOrder
import java.util.*

data class StockOrderAggregate(
        val rateInSek: Double,
        val displayName: String,
        val stockSymbol: String,
        val currency: Currency,
        private var accumulatedPurchases: Double = 0.0,
        private var accumulatedSales: Double = 0.0,
        private var buyQuantity: Int = 0,
        private var sellQuantity: Int = 0,
        private var acquisitionValue: Double = 0.0,
) {

    fun aggregate(stockOrder: StockOrder) {
        when (stockOrder.orderAction) {
            "Buy" -> {
                acquisitionValue = ((buyQuantity - sellQuantity) * acquisitionValue +
                        stockOrder.quantity * stockOrder.getAcquisitionValue(rateInSek)) /
                        ((buyQuantity - sellQuantity) + stockOrder.quantity)
                buyQuantity += stockOrder.quantity
                stockOrder.getAcquisitionValue(rateInSek)
                accumulatedPurchases += stockOrder.pricePerStock * stockOrder.quantity +
                        stockOrder.commissionFee / rateInSek
            }
            "Sell" -> {
                sellQuantity += stockOrder.quantity
                if (buyQuantity - sellQuantity == 0) {
                    acquisitionValue = 0.0
                }
                accumulatedSales += stockOrder.pricePerStock * stockOrder.quantity + stockOrder.commissionFee / rateInSek
            }
            else -> {
                throw IllegalStateException("Invalid stock order action: ${stockOrder.orderAction}")
            }
        }
    }

    fun getQuantity(): Int {
        return buyQuantity - sellQuantity
    }

    fun getAcquisitionValue(): Double {
        return acquisitionValue
    }

    fun getProfit(currentStockPrice: Double): Double {
        return if (buyQuantity == 0) {
            0.0
        } else {
            val realizedProfit = accumulatedSales - (accumulatedPurchases / buyQuantity) * sellQuantity
            val currentQuantity = getQuantity()
            val unrealizedProfit = currentStockPrice * currentQuantity - currentQuantity * getAcquisitionValue()
            realizedProfit + unrealizedProfit
        }
    }

}
