package com.sundbybergsit.cromfortune.main.stocks

import android.content.Context
import com.sundbybergsit.cromfortune.domain.AssetCatalog
import com.sundbybergsit.cromfortune.domain.AssetEvent
import com.sundbybergsit.cromfortune.domain.AssetEventApi
import com.sundbybergsit.cromfortune.domain.AssetTransactionApi
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.StockSplitApi
import com.sundbybergsit.cromfortune.domain.TransactionAction
import java.math.BigDecimal

class AssetEventRepository(
    context: Context,
    portfolioName: String,
    private val transactionApi: AssetTransactionApi = AssetTransactionRepository(context, portfolioName),
    private val stockSplitApi: StockSplitApi = StockSplitRepository(context, porfolioName = portfolioName)
) : AssetEventApi {

    override fun currentQuantity(assetId: String): BigDecimal {
        var quantity = BigDecimal.ZERO
        list(assetId).sortedBy(AssetEvent::dateInMillis).forEach { event ->
            event.transaction?.let { transaction ->
                quantity += if (transaction.action == TransactionAction.BUY) transaction.quantity else -transaction.quantity
                require(quantity >= BigDecimal.ZERO) { "Sale exceeds available quantity for $assetId" }
            }
            event.stockSplit?.let { split ->
                quantity = if (split.reverse) {
                    quantity.divideToIntegralValue(split.quantity.toBigDecimal())
                } else {
                    quantity.multiply(split.quantity.toBigDecimal())
                }
            }
        }
        return quantity
    }

    override fun assetIds(): Set<String> = transactionApi.assetIds()

    override fun isEmpty(): Boolean = transactionApi.isEmpty()

    override fun list(assetId: String): Set<AssetEvent> {
        val transactions = transactionApi.list(assetId)
        val type = transactions.firstOrNull()?.assetType ?: AssetCatalog.findById(assetId)?.type ?: return emptySet()
        val events = transactions.mapTo(mutableSetOf()) { transaction ->
            AssetEvent(assetId, type, transaction = transaction, dateInMillis = transaction.dateInMillis)
        }
        if (type == AssetType.STOCK) {
            val symbol = transactions.firstOrNull()?.symbol ?: AssetCatalog.findById(assetId)?.symbol
            symbol?.let {
                events += stockSplitApi.list(it).map { split ->
                    AssetEvent(assetId, type, stockSplit = split, dateInMillis = split.dateInMillis)
                }
            }
        }
        return events
    }

    override fun remove(assetId: String) {
        val asset = AssetCatalog.findById(assetId)
        val persistedTransaction = transactionApi.list(assetId).firstOrNull()
        transactionApi.remove(assetId)
        val assetType = asset?.type ?: persistedTransaction?.assetType
        val symbol = asset?.symbol ?: persistedTransaction?.symbol
        if (assetType == AssetType.STOCK && symbol != null) stockSplitApi.remove(symbol)
    }
}
