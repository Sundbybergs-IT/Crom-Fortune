package com.sundbybergsit.cromfortune.algorithm.core

import com.sundbybergsit.cromfortune.algorithm.api.AssetTransactionCommand
import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.AssetTransactionApi
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.TransactionAction
import java.math.BigDecimal

sealed class AssetCommand(
    override val assetId: String,
    private val assetType: AssetType,
    private val symbol: String,
    private val displayName: String,
    private val quoteCurrencyCode: String,
    private val dateInMillis: Long,
    override val unitPrice: BigDecimal,
    override val quantity: BigDecimal,
    override val commissionFee: BigDecimal,
    private val action: TransactionAction
) : AssetTransactionCommand {

    init {
        require(quantity > BigDecimal.ZERO) { "Quantity must be positive" }
        require(unitPrice > BigDecimal.ZERO) { "Unit price must be positive" }
        require(commissionFee >= BigDecimal.ZERO) { "Commission fee must not be negative" }
    }

    final override fun execute(item: AssetTransactionApi) {
        if (action == TransactionAction.SELL) {
            require(quantity <= item.currentQuantity(assetId)) { "Sale exceeds available quantity for $assetId" }
        }
        val transaction = AssetTransaction(
            assetId = assetId,
            assetType = assetType,
            symbol = symbol,
            displayName = displayName,
            quoteCurrencyCode = quoteCurrencyCode,
            action = action,
            dateInMillis = dateInMillis,
            unitPrice = unitPrice,
            commissionFee = commissionFee,
            quantity = quantity
        )
        item.putAll(assetId, item.list(assetId) + transaction)
    }
}

class BuyAssetCommand(
    assetId: String,
    assetType: AssetType,
    symbol: String,
    displayName: String,
    quoteCurrencyCode: String,
    dateInMillis: Long,
    unitPrice: BigDecimal,
    quantity: BigDecimal,
    commissionFee: BigDecimal = BigDecimal.ZERO
) : AssetCommand(assetId, assetType, symbol, displayName, quoteCurrencyCode, dateInMillis, unitPrice, quantity,
    commissionFee, TransactionAction.BUY)

class SellAssetCommand(
    assetId: String,
    assetType: AssetType,
    symbol: String,
    displayName: String,
    quoteCurrencyCode: String,
    dateInMillis: Long,
    unitPrice: BigDecimal,
    quantity: BigDecimal,
    commissionFee: BigDecimal = BigDecimal.ZERO
) : AssetCommand(assetId, assetType, symbol, displayName, quoteCurrencyCode, dateInMillis, unitPrice, quantity,
    commissionFee, TransactionAction.SELL)
