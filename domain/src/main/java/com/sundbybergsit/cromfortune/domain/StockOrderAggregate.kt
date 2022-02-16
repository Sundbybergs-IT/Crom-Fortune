package com.sundbybergsit.cromfortune.domain

import java.util.*

data class StockOrderAggregate(
    val rateInSek: Double,
    val displayName: String,
    val stockSymbol: String,
    val currency: Currency,
    private var accumulatedPurchases: Double = 0.0,
    private var accumulatedSales: Double = 0.0,
    private var aggregateBuyQuantity: Int = 0,
    private var aggregateSellQuantity: Int = 0,
    private var aggregateAcquisitionValue: Double = 0.0,
    val events: MutableList<StockEvent> = mutableListOf()
) {

    fun aggregate(stockEvent: StockEvent) {
        val stockEventToProcess = if (stockEvent.stockSplit != null) {
            val previousOrders = events.filter { it.stockOrder != null && it.dateInMillis < stockEvent.dateInMillis }
            val oldAggregate = StockOrderAggregate(rateInSek, displayName, stockSymbol, currency)
            for (previousOrder in previousOrders) {
                oldAggregate.aggregate(previousOrder)
            }
            // Create new fake stock buy/sell order event corresponding to the split
            StockEvent(
                oldAggregate.getCorrespondingSplitStockOrder(stockEvent.stockSplit),
                null,
                stockEvent.dateInMillis
            )
        } else {
            stockEvent
        }
        events.add(stockEventToProcess)
        with(checkNotNull(stockEventToProcess.stockOrder)) {
            when (orderAction) {
                "Buy" -> {
                    buy()
                }
                "Sell" -> {
                    sell()
                }
                else -> {
                    throw IllegalStateException("Invalid stock order action: $orderAction")
                }
            }
        }
    }

    private fun getCorrespondingSplitStockOrder(stockSplit: StockSplit): StockOrder {
        val netQuantity = aggregateBuyQuantity - aggregateSellQuantity
        return if (stockSplit.reverse) {
            StockOrder(
                orderAction = "Sell",
                currency = currency.currencyCode,
                dateInMillis = stockSplit.dateInMillis,
                name = stockSymbol,
                pricePerStock = 0.0,
                commissionFee = 0.0,
                quantity = netQuantity - netQuantity / stockSplit.quantity
            )
        } else {
            StockOrder(
                orderAction = "Buy",
                currency = currency.currencyCode,
                dateInMillis = stockSplit.dateInMillis,
                name = stockSymbol,
                pricePerStock = 0.0,
                commissionFee = 0.0,
                quantity = netQuantity * stockSplit.quantity - netQuantity
            )
        }

    }

    private fun StockOrder.buy() {
        aggregateAcquisitionValue = getCalculatedAcquisitionValue()
        aggregateBuyQuantity += this.quantity
        this.getAcquisitionValue(this@StockOrderAggregate.rateInSek)
        accumulatedPurchases += this.pricePerStock * this.quantity +
                this.commissionFee / rateInSek
    }

    private fun StockOrder.getCalculatedAcquisitionValue() =
        ((aggregateBuyQuantity - aggregateSellQuantity) * aggregateAcquisitionValue +
                this.quantity * this.getAcquisitionValue(this@StockOrderAggregate.rateInSek)) /
                ((aggregateBuyQuantity - aggregateSellQuantity) + this.quantity)

    private fun StockOrder.sell() {
        val previousAggregateAcquisitionValue =
            aggregateAcquisitionValue * (aggregateBuyQuantity - aggregateSellQuantity)
        aggregateSellQuantity += this.quantity
        aggregateAcquisitionValue = if (aggregateBuyQuantity - aggregateSellQuantity == 0) {
            0.0
        } else {
            (previousAggregateAcquisitionValue - (this.quantity * this.pricePerStock) / (getQuantity() + quantity)) /
                    getQuantity()
        }
        accumulatedSales += this.pricePerStock * this.quantity + this.commissionFee / rateInSek
    }

    fun getQuantity(): Int {
        return aggregateBuyQuantity - aggregateSellQuantity
    }

    fun getAcquisitionValue(): Double {
        return aggregateAcquisitionValue
    }

    fun getProfit(currentStockPrice: Double): Double {
        return if (aggregateBuyQuantity == 0) {
            0.0
        } else {
            val realizedProfit = accumulatedSales - accumulatedPurchases
            val currentQuantity = getQuantity()
            val unrealizedProfit = currentStockPrice * currentQuantity
            realizedProfit + unrealizedProfit
        }
    }

}
