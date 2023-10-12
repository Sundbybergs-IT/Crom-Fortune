package com.sundbybergsit.cromfortune.main.stocks

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderApi
import com.sundbybergsit.cromfortune.main.Taggable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// FIXME: Convert to datastore, https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
class StockOrderRepository(
    context: Context,
    porfolioName: String,
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(porfolioName, Context.MODE_PRIVATE),
) : StockOrderApi, Taggable {

    override fun count(stockSymbol: String): Int {
        val list: Set<StockOrder> = list(stockSymbol)
        var count = 0
        for (stockOrder in list) {
            when (stockOrder.orderAction) {
                "Buy" -> {
                    count += stockOrder.quantity
                }
                "Sell" -> {
                    count -= stockOrder.quantity
                }
                else -> {
                    throw IllegalStateException()
                }
            }
        }
        return count
    }

    override fun countAll(): Int {
        return sharedPreferences.all.keys.size
    }

    override fun listOfStockNames(): Iterable<String> {
        return sharedPreferences.all.keys
    }

    override fun isEmpty(): Boolean {
        return sharedPreferences.all.isEmpty()
    }

    override fun list(stockSymbol: String): Set<StockOrder> {
        Log.i(TAG, "list([$stockSymbol])")
        val serializedOrders = sharedPreferences.getStringSet(stockSymbol, emptySet()) as Set<String>
        val result = mutableSetOf<StockOrder>()
        for (serializedOrder in serializedOrders) {
            val setOfStockOrders : Set<StockOrder> = Json.decodeFromString(serializedOrder)
            result.addAll(setOfStockOrders)
        }
        return result
    }

    override fun putAll(stockSymbol: String, stockOrders: Set<StockOrder>) {
        Log.i(TAG, "putAll([$stockSymbol], [$stockOrders])")
        val serializedStockOrders = mutableSetOf<String>()
        // TODO: Yes, accidentally wrapped a collection too much... Must make upgrade script
        serializedStockOrders.add(Json.encodeToString(stockOrders))
        sharedPreferences.edit().putStringSet(stockSymbol, serializedStockOrders).apply()
    }

    override fun putReplacingAll(stockSymbol: String, stockOrder: StockOrder) {
        Log.i(TAG, "putReplacingAll([$stockSymbol], [$stockOrder])")
        putAll(stockSymbol, setOf(stockOrder))
    }

    override fun remove(stockSymbol: String) {
        Log.i(TAG, "remove([$stockSymbol])")
        sharedPreferences.edit().remove(stockSymbol).apply()
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
            sharedPreferences.edit().putStringSet(stockOrder.name, serializedStockOrders).apply()
        }
    }

}
