package com.sundbybergsit.cromfortune.domain

import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.Currency

@Serializable
data class AssetTransaction(
    val assetId: String,
    val assetType: AssetType,
    val symbol: String,
    val displayName: String,
    val quoteCurrencyCode: String,
    val action: TransactionAction,
    val dateInMillis: Long,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val unitPrice: BigDecimal,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val commissionFee: BigDecimal = BigDecimal.ZERO,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val quantity: BigDecimal,
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION
) {
    init {
        require(assetId.isNotBlank()) { "Asset ID must not be blank" }
        require(symbol.isNotBlank()) { "Asset symbol must not be blank" }
        require(quoteCurrencyCode.isNotBlank()) { "Quote currency must not be blank" }
        require(quantity > BigDecimal.ZERO) { "Quantity must be positive" }
        require(unitPrice > BigDecimal.ZERO) { "Unit price must be positive" }
        require(commissionFee >= BigDecimal.ZERO) { "Commission fee must not be negative" }
    }

    val quoteCurrency: Currency get() = Currency.getInstance(quoteCurrencyCode)

    companion object {
        const val CURRENT_SCHEMA_VERSION = 3

        fun fromStockOrder(order: StockOrder): AssetTransaction {
            val catalogAsset = AssetCatalog.findById(order.assetId)
            return AssetTransaction(
                assetId = order.assetId,
                assetType = order.assetType,
                symbol = order.name,
                displayName = catalogAsset?.displayName ?: order.name,
                quoteCurrencyCode = order.currency,
                action = when (order.orderAction) {
                    "Buy" -> TransactionAction.BUY
                    "Sell" -> TransactionAction.SELL
                    else -> error("Illegal order action: ${order.orderAction}")
                },
                dateInMillis = order.dateInMillis,
                unitPrice = order.pricePerStock.toBigDecimal(),
                commissionFee = order.commissionFee.toBigDecimal(),
                quantity = order.quantity
            )
        }
    }

    fun toStockOrder(): StockOrder {
        require(assetType == AssetType.STOCK) { "Only stock transactions can use the StockOrder adapter" }
        return StockOrder(
            orderAction = if (action == TransactionAction.BUY) "Buy" else "Sell",
            currency = quoteCurrencyCode,
            dateInMillis = dateInMillis,
            name = symbol,
            pricePerStock = unitPrice.toDouble(),
            commissionFee = commissionFee.toDouble(),
            quantity = quantity,
            assetId = assetId,
            assetType = assetType
        )
    }
}
