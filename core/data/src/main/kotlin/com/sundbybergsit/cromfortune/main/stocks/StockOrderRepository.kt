package com.sundbybergsit.cromfortune.main.stocks

import android.content.Context
import android.util.Log
import com.sundbybergsit.cromfortune.domain.AssetCatalog
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderApi
import com.sundbybergsit.cromfortune.main.Taggable
import com.sundbybergsit.cromfortune.main.db.CromFortuneDatabase
import com.sundbybergsit.cromfortune.main.db.StockOrderDao
import com.sundbybergsit.cromfortune.main.db.StockOrderEntity

class StockOrderRepository(
    context: Context,
    private val portfolioName: String,
    private val dao: StockOrderDao = CromFortuneDatabase.getInstance(context).stockOrderDao()
) : StockOrderApi, Taggable {

    override fun count(stockSymbol: String): Int {
        val list: Set<StockOrder> = list(stockSymbol)
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
        return dao.getOrdersForPortfolio(portfolioName).map { it.name }.distinct().mapNotNull { name ->
            val assetId = AssetCatalog.findBySymbol(AssetType.STOCK, name)?.id ?: "stock:$name"
            when {
                assetId.startsWith("stock:") -> AssetCatalog.findById(assetId)?.symbol ?: name
                assetId.startsWith("crypto:") -> null
                else -> name
            }
        }
    }

    override fun isEmpty(): Boolean = dao.getOrdersForPortfolio(portfolioName).isEmpty()

    override fun list(stockSymbol: String): Set<StockOrder> {
        Log.i(TAG, "list([$stockSymbol])")
        val entities = dao.getOrdersForAsset(portfolioName, stockSymbol)
        return entities.map { it.toDomain() }.toSet()
    }

    override fun putAll(stockSymbol: String, stockOrders: Set<StockOrder>) {
        Log.i(TAG, "putAll([$stockSymbol], [$stockOrders])")
        val assetId = AssetCatalog.findBySymbol(AssetType.STOCK, stockSymbol)?.id ?: "stock:$stockSymbol"
        dao.deleteForAsset(portfolioName, assetId)
        dao.deleteForAsset(portfolioName, stockSymbol)
        val entities = stockOrders.map { StockOrderEntity.fromDomain(portfolioName, it) }
        dao.insertAll(entities)
    }

    override fun putReplacingAll(stockSymbol: String, stockOrder: StockOrder) {
        Log.i(TAG, "putReplacingAll([$stockSymbol], [$stockOrder])")
        putAll(stockSymbol, setOf(stockOrder))
    }

    override fun remove(stockSymbol: String) {
        Log.i(TAG, "remove([$stockSymbol])")
        val assetId = AssetCatalog.findBySymbol(AssetType.STOCK, stockSymbol)?.id ?: "stock:$stockSymbol"
        dao.deleteForAsset(portfolioName, assetId)
        dao.deleteForAsset(portfolioName, stockSymbol)
    }

    override fun remove(stockOrder: StockOrder) {
        Log.i(TAG, "remove([$stockOrder])")
        val stockOrders = list(stockOrder.name).toMutableSet()
        stockOrders.remove(stockOrder)
        if (stockOrders.isEmpty()) {
            remove(stockOrder.name)
        } else {
            putAll(stockOrder.name, stockOrders)
        }
    }

}
