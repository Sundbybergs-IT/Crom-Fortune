package com.sundbybergsit.cromfortune.main.stocks

import android.content.Context
import android.util.Log
import com.sundbybergsit.cromfortune.domain.AssetCatalog
import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.AssetTransactionApi
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderApi
import com.sundbybergsit.cromfortune.main.Taggable

class StockOrderRepository(
    context: Context,
    private val portfolioName: String,
    private val assetTransactionApi: AssetTransactionApi = AssetTransactionRepository(context, portfolioName)
) : StockOrderApi, Taggable {

    override fun count(stockSymbol: String): Int {
        val list: List<StockOrder> = list(stockSymbol)
        var count = 0
        for (stockOrder in list) {
            when (stockOrder.orderAction) {
                "Buy" -> {
                    count += stockOrder.quantity.intValueExact()
                }
                "Sell" -> {
                    count -= stockOrder.quantity.intValueExact()
                }
                "Dividend" -> {
                    // Dividends do not alter stock count
                }
                else -> {
                    throw IllegalStateException()
                }
            }
        }
        return count
    }

    override fun countAll(): Int = listOfAssetNames().count()

    override fun listOfAssetNames(): Iterable<String> {
        return assetTransactionApi.assetIds().mapNotNull { assetId ->
            val asset = AssetCatalog.findById(assetId)
            if (asset?.type == AssetType.STOCK || assetId.startsWith("stock:")) {
                asset?.symbol ?: assetId.removePrefix("stock:")
            } else {
                null
            }
        }.distinct()
    }

    override fun isEmpty(): Boolean = assetTransactionApi.isEmpty()

    override fun list(stockSymbol: String): List<StockOrder> {
        Log.i(TAG, "list([$stockSymbol])")
        val assetId = AssetCatalog.findBySymbol(AssetType.STOCK, stockSymbol)?.id ?: "stock:$stockSymbol"
        return assetTransactionApi.list(assetId)
            .filter { it.assetType == AssetType.STOCK }
            .map { it.toStockOrder() }
    }

    override fun putAll(stockSymbol: String, stockOrders: Collection<StockOrder>) {
        Log.i(TAG, "putAll([$stockSymbol], [$stockOrders])")
        val assetId = AssetCatalog.findBySymbol(AssetType.STOCK, stockSymbol)?.id ?: "stock:$stockSymbol"
        val transactions = stockOrders.map { AssetTransaction.fromStockOrder(it) }
        assetTransactionApi.putAll(assetId, transactions)
    }

    override fun putReplacingAll(stockSymbol: String, stockOrder: StockOrder) {
        Log.i(TAG, "putReplacingAll([$stockSymbol], [$stockOrder])")
        putAll(stockSymbol, setOf(stockOrder))
    }

    override fun remove(stockSymbol: String) {
        Log.i(TAG, "remove([$stockSymbol])")
        val assetId = AssetCatalog.findBySymbol(AssetType.STOCK, stockSymbol)?.id ?: "stock:$stockSymbol"
        assetTransactionApi.remove(assetId)
    }

    override fun remove(stockOrder: StockOrder) {
        Log.i(TAG, "remove([$stockOrder])")
        val transaction = AssetTransaction.fromStockOrder(stockOrder)
        assetTransactionApi.remove(transaction)
    }

}
