package com.sundbybergsit.cromfortune.main.stocks

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.sundbybergsit.cromfortune.domain.AssetCatalog
import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderApi
import com.sundbybergsit.cromfortune.main.Taggable
import kotlinx.serialization.json.Json

// FIXME: Convert to datastore, https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
class StockOrderRepository(
    context: Context,
    portfolioName: String,
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(portfolioName, Context.MODE_PRIVATE),
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
                else -> {
                    throw IllegalStateException()
                }
            }
        }
        return count
    }

    override fun countAll(): Int {
        return listOfStockNames().count()
    }

    override fun listOfStockNames(): Iterable<String> {
        return sharedPreferences.all.keys.mapNotNull { key ->
            when {
                key.startsWith("stock:") -> AssetCatalog.findById(key)?.symbol ?: key.removePrefix("stock:")
                key.startsWith("crypto:") -> null
                else -> key
            }
        }
    }

    override fun isEmpty(): Boolean {
        return listOfStockNames().none()
    }

    override fun list(stockSymbol: String): Set<StockOrder> {
        Log.i(TAG, "list([$stockSymbol])")
        val assetId = AssetCatalog.findBySymbol(AssetType.STOCK, stockSymbol)?.id ?: "stock:$stockSymbol"
        val serializedOrders = sharedPreferences.getStringSet(assetId, null)
            ?: sharedPreferences.getStringSet(stockSymbol, emptySet()).orEmpty()
        val result = mutableSetOf<StockOrder>()
        for (serializedOrder in serializedOrders) {
            try {
                val setOfStockOrders: Set<StockOrder> = Json.decodeFromString(serializedOrder)
                result.addAll(setOfStockOrders)
            } catch (e: Exception) {
                try {
                    val transactions: Set<AssetTransaction> = Json.decodeFromString(serializedOrder)
                    result.addAll(transactions.filter { it.assetType == AssetType.STOCK }.map(AssetTransaction::toStockOrder))
                } catch (transactionError: Exception) {
                    Log.e(TAG, "Failed to decode $serializedOrder", transactionError)
                }
            }
        }
        return result
    }

    override fun putAll(stockSymbol: String, stockOrders: Set<StockOrder>) {
        Log.i(TAG, "putAll([$stockSymbol], [$stockOrders])")
        val serializedStockOrders = mutableSetOf<String>()
        // TODO: Yes, accidentally wrapped a collection too much... Must make upgrade script
        serializedStockOrders.add(Json.encodeToString(stockOrders))
        val assetId = AssetCatalog.findBySymbol(AssetType.STOCK, stockSymbol)?.id ?: "stock:$stockSymbol"
        sharedPreferences.edit().putStringSet(assetId, serializedStockOrders).remove(stockSymbol).apply()
    }

    override fun putReplacingAll(stockSymbol: String, stockOrder: StockOrder) {
        Log.i(TAG, "putReplacingAll([$stockSymbol], [$stockOrder])")
        putAll(stockSymbol, setOf(stockOrder))
    }

    override fun remove(stockSymbol: String) {
        Log.i(TAG, "remove([$stockSymbol])")
        val assetId = AssetCatalog.findBySymbol(AssetType.STOCK, stockSymbol)?.id ?: "stock:$stockSymbol"
        sharedPreferences.edit().remove(assetId).remove(stockSymbol).apply()
    }

    override fun remove(stockOrder: StockOrder) {
        Log.i(TAG, "remove([$stockOrder])")
        val stockOrders =  list(stockOrder.name).toMutableSet()
        stockOrders.remove(stockOrder)
        if (stockOrders.isEmpty()) {
            remove(stockOrder.name)
        } else {
            val serializedStockOrders = mutableSetOf<String>()
            serializedStockOrders.add(Json.encodeToString(stockOrders))
            sharedPreferences.edit().putStringSet(stockOrder.assetId, serializedStockOrders).remove(stockOrder.name).apply()
        }
    }

}
