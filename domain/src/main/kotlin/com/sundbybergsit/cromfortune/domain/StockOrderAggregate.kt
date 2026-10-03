package com.sundbybergsit.cromfortune.domain

import java.math.BigDecimal
import java.util.Currency

data class StockOrderAggregate(
    val rateInSek: Double,
    val displayName: String,
    val stockSymbol: String,
    val currency: Currency,
    private var accumulatedPurchases: Double = 0.0,
    private var accumulatedSales: Double = 0.0,
    private var aggregateBuyQuantity: BigDecimal = BigDecimal.ZERO,
    private var aggregateSellQuantity: BigDecimal = BigDecimal.ZERO,
    private var aggregateAcquisitionValue: Double = 0.0,
    private var holdingCost: Double = 0.0,
    private var realizedProfit: Double = 0.0,
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
                quantity = netQuantity - netQuantity.divideToIntegralValue(stockSplit.quantity.toBigDecimal())
            )
        } else {
            StockOrder(
                orderAction = "Buy",
                currency = currency.currencyCode,
                dateInMillis = stockSplit.dateInMillis,
                name = stockSymbol,
                pricePerStock = 0.0,
                commissionFee = 0.0,
                quantity = netQuantity * stockSplit.quantity.toBigDecimal() - netQuantity
            )
        }

    }

    private fun StockOrder.buy() {
        val buyCost = this.pricePerStock * this.quantity.toDouble() + this.commissionFee / rateInSek
        holdingCost += buyCost
        accumulatedPurchases += buyCost
        aggregateBuyQuantity += this.quantity
        val currentQty = (aggregateBuyQuantity - aggregateSellQuantity).toDouble()
        aggregateAcquisitionValue = if (currentQty > 0.0) holdingCost / currentQty else 0.0
    }

    private fun StockOrder.sell() {
        val currentQtyBefore = (aggregateBuyQuantity - aggregateSellQuantity).toDouble()
        val soldQty = this.quantity.toDouble()
        val netSaleIncome = soldQty * this.pricePerStock - this.commissionFee / rateInSek
        val currentGav = if (currentQtyBefore > 0.0) holdingCost / currentQtyBefore else 0.0
        val costOfSold = if (this.pricePerStock == 0.0) 0.0 else soldQty * currentGav
        realizedProfit += (netSaleIncome - costOfSold)
        holdingCost -= costOfSold
        accumulatedPurchases += (this.commissionFee / rateInSek)
        aggregateSellQuantity += this.quantity
        if (aggregateSellQuantity > aggregateBuyQuantity) {
            throw IllegalStateException("Number of sold stocks exceed available quantity!")
        }
        val currentQtyAfter = (aggregateBuyQuantity - aggregateSellQuantity).toDouble()
        if (currentQtyAfter <= 0.0) {
            holdingCost = 0.0
            aggregateAcquisitionValue = 0.0
        } else {
            aggregateAcquisitionValue = holdingCost / currentQtyAfter
        }
        accumulatedSales += soldQty * this.pricePerStock
    }

    fun getExactQuantity(): BigDecimal = aggregateBuyQuantity - aggregateSellQuantity

    @Deprecated("Use getExactQuantity()")
    fun getQuantity(): Int = getExactQuantity().intValueExact()

    fun getAcquisitionValue(): Double {
        return aggregateAcquisitionValue
    }

    fun getRealizedProfit(): Double {
        return realizedProfit
    }

    fun getProfit(currentStockPrice: Double): Double {
        return if (aggregateBuyQuantity == BigDecimal.ZERO) {
            0.0
        } else {
            val currentQuantity = getExactQuantity().toDouble()
            val unrealizedProfit = (currentStockPrice * currentQuantity) - holdingCost
            realizedProfit + unrealizedProfit
        }
    }

}
