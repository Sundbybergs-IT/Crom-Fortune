package com.sundbybergsit.cromfortune.ui.home

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
) {

    fun aggregate(stockOrder: StockOrder) {
        when (stockOrder.orderAction) {
            "Buy" -> {
                buyQuantity += stockOrder.quantity
                accumulatedPurchases += stockOrder.pricePerStock * stockOrder.quantity +
                        stockOrder.commissionFee / rateInSek
            }
            "Sell" -> {
                sellQuantity += stockOrder.quantity
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
        return accumulatedPurchases / buyQuantity
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
