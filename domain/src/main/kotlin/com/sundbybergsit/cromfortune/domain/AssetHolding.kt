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
    private var boughtQuantity = BigDecimal.ZERO
    private var soldQuantity = BigDecimal.ZERO
    private val mutableEvents = mutableListOf<AssetEvent>()

    val events: List<AssetEvent> get() = mutableEvents.toList()
    val quantity: BigDecimal get() = boughtQuantity - soldQuantity

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
                boughtQuantity += transaction.quantity
                purchases += grossValue + feeInQuoteCurrency
            }
            TransactionAction.SELL -> {
                require(transaction.quantity <= quantity) { "Sale exceeds available quantity" }
                soldQuantity += transaction.quantity
                sales += grossValue - feeInQuoteCurrency
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
        (purchases - sales).divide(quantity, MathContext.DECIMAL128)
    }

    fun profit(currentUnitPrice: BigDecimal): BigDecimal =
        sales - purchases + currentUnitPrice.multiply(quantity)
}
