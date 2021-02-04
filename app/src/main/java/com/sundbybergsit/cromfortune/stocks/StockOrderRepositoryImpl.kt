package com.sundbybergsit.cromfortune.stocks

import android.content.Context
import android.content.SharedPreferences
import com.sundbybergsit.cromfortune.ui.home.StockOrder
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

const val PREFERENCES_NAME = "Stocks"

class StockOrderRepositoryImpl(
        context: Context,
        private val sharedPreferences: SharedPreferences =
                context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE),
) : StockOrderRepository {

    override fun count(stockName: String): Int {
        val list: Set<StockOrder> = list(stockName)
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

    override fun listOfStockNames(): Iterable<String> {
        return sharedPreferences.all.keys
    }

    override fun isEmpty(): Boolean {
        return sharedPreferences.all.isEmpty()
    }

    override fun list(stockName: String): Set<StockOrder> {
        val serializedOrders = sharedPreferences.getStringSet(stockName, emptySet()) as Set<String>
        val result = mutableSetOf<StockOrder>()
        for (serializedOrder in serializedOrders) {
            val setOfStockOrders : Set<StockOrder> = Json.decodeFromString(serializedOrder)
            result.addAll(setOfStockOrders)
        }
        return result
    }

    override fun putAll(stockName: String, stockOrders: Set<StockOrder>) {
        val serializedStockOrders = mutableSetOf<String>()
        // TODO: Yes, accidentally wrapped a collection too much... Must make upgrade script
        serializedStockOrders.add(Json.encodeToString(stockOrders))
        sharedPreferences.edit().putStringSet(stockName, serializedStockOrders).apply()
    }

    override fun put(stockName: String, stockOrder: StockOrder) {
        putAll(stockName, setOf(stockOrder))
    }

    override fun remove(stockName: String) {
        sharedPreferences.edit().remove(stockName).apply()
    }

    override fun remove(stockOrder: StockOrder) {
        val stockOrders =  list(stockOrder.name).toMutableSet()
        stockOrders.remove(stockOrder)
        val serializedStockOrders = mutableSetOf<String>()
        serializedStockOrders.add(Json.encodeToString(stockOrders))
        sharedPreferences.edit().putStringSet(stockOrder.name, serializedStockOrders).apply()
    }

}
