package com.sundbybergsit.cromfortune.domain

import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Currency

class AssetHolding(
    val assetId: String,
    val assetType: AssetType,
    val symbol: String,
    val displayName: String,
    val quoteCurrency: Currency,
    val rateInSek: BigDecimal
) {
    init {
        require(rateInSek > BigDecimal.ZERO) { "Currency rate must be positive" }
    }

    private var purchases = BigDecimal.ZERO
    private var sales = BigDecimal.ZERO
    private var holdingCost = BigDecimal.ZERO
    private var accumulatedRealizedProfit = BigDecimal.ZERO
    private var accumulatedDividends = BigDecimal.ZERO
    private var boughtQuantity = BigDecimal.ZERO
    private var soldQuantity = BigDecimal.ZERO
    private val mutableEvents = mutableListOf<AssetEvent>()

    val events: List<AssetEvent> get() = mutableEvents.toList()
    val quantity: BigDecimal get() = boughtQuantity - soldQuantity
    val totalDividends: BigDecimal get() = accumulatedDividends

    fun quantityAt(dateInMillis: Long): BigDecimal {
        var qty = BigDecimal.ZERO
        mutableEvents.filter { it.dateInMillis <= dateInMillis }
            .sortedWith(assetEventChronologicalComparator)
            .forEach { event ->
                event.transaction?.let { tx ->
                    when (tx.action) {
                        TransactionAction.BUY -> qty += tx.quantity
                        TransactionAction.SELL -> qty -= tx.quantity
                        TransactionAction.DIVIDEND -> { /* Dividends do not alter quantity */ }
                    }
                }
                event.stockSplit?.let { split ->
                    qty = if (split.reverse) {
                        qty.divide(split.quantity.toBigDecimal(), 16, RoundingMode.DOWN)
                    } else {
                        qty.multiply(split.quantity.toBigDecimal())
                    }
                }
            }
        return qty
    }

    fun quantityAtExDate(dateInMillis: Long): BigDecimal {
        var qty = BigDecimal.ZERO
        mutableEvents.filter { it.dateInMillis < dateInMillis }
            .sortedWith(assetEventChronologicalComparator)
            .forEach { event ->
                event.transaction?.let { tx ->
                    when (tx.action) {
                        TransactionAction.BUY -> qty += tx.quantity
                        TransactionAction.SELL -> qty -= tx.quantity
                        TransactionAction.DIVIDEND -> { /* Dividends do not alter quantity */ }
                    }
                }
                event.stockSplit?.let { split ->
                    qty = if (split.reverse) {
                        qty.divide(split.quantity.toBigDecimal(), 16, RoundingMode.DOWN)
                    } else {
                        qty.multiply(split.quantity.toBigDecimal())
                    }
                }
            }
        return qty
    }

    fun aggregate(event: AssetEvent) {
        require(event.assetId == assetId) { "Event belongs to ${event.assetId}, expected $assetId" }
        require(event.assetType == assetType) { "Event type does not match holding type" }
        event.transaction?.let(::aggregateTransaction)
        event.stockSplit?.let(::aggregateSplit)
        mutableEvents += event
    }

    private fun aggregateTransaction(transaction: AssetTransaction) {
        val grossValue = transaction.unitPrice.multiply(transaction.quantity)
        val feeInQuoteCurrency = transaction.commissionFee.divide(rateInSek, MathContext.DECIMAL128)
        when (transaction.action) {
            TransactionAction.BUY -> {
                val buyCost = grossValue + feeInQuoteCurrency
                boughtQuantity += transaction.quantity
                holdingCost += buyCost
                purchases += buyCost
            }
            TransactionAction.SELL -> {
                require(transaction.quantity <= quantity) { "Sale exceeds available quantity" }
                val netSaleIncome = grossValue - feeInQuoteCurrency
                val currentGav = if (quantity.signum() > 0) holdingCost.divide(quantity, MathContext.DECIMAL128) else BigDecimal.ZERO
                val costOfSold = currentGav.multiply(transaction.quantity)
                accumulatedRealizedProfit += netSaleIncome - costOfSold
                soldQuantity += transaction.quantity
                if (quantity.signum() == 0) {
                    holdingCost = BigDecimal.ZERO
                } else {
                    holdingCost = holdingCost.subtract(costOfSold)
                }
                sales += netSaleIncome
            }
            TransactionAction.DIVIDEND -> {
                val netIncome = grossValue - feeInQuoteCurrency
                accumulatedDividends += netIncome
            }
        }
    }

    private fun aggregateSplit(split: StockSplit) {
        require(assetType == AssetType.STOCK) { "Splits are only supported for stocks" }
        if (split.reverse) {
            val newQuantity = quantity.divide(split.quantity.toBigDecimal(), 16, RoundingMode.DOWN)
            soldQuantity += quantity - newQuantity
        } else {
            boughtQuantity += quantity.multiply(split.quantity.toBigDecimal() - BigDecimal.ONE)
        }
    }

    fun acquisitionValue(): BigDecimal = if (quantity.signum() == 0) {
        BigDecimal.ZERO
    } else {
        holdingCost.divide(quantity, MathContext.DECIMAL128)
    }

    fun realizedProfit(): BigDecimal = accumulatedRealizedProfit

    fun profit(currentUnitPrice: BigDecimal): BigDecimal =
        accumulatedRealizedProfit + accumulatedDividends + currentUnitPrice.multiply(quantity) - holdingCost
}
