package com.sundbybergsit.cromfortune.ui.home

import android.content.Context
import com.sundbybergsit.cromfortune.stocks.StocksPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class SellStockCommand(private val context: Context, private val currentTimeInMillis: Long,
                       private val currency: Currency, private val name: String, private val pricePerStock: Double,
                       private val quantity: Int) : Command {

    override fun execute() {
        val sharedPreferences = context.getSharedPreferences(StocksPreferences.PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (sharedPreferences.contains(name)) {
            val stockOrders: MutableSet<String> = sharedPreferences.getStringSet(name, null) as MutableSet<String>
            stockOrders.add(Json.encodeToString(StockOrder(orderAction = "Sell", currency = currency.toString(),
                    dateInMillis = currentTimeInMillis, name = name, pricePerStock = pricePerStock,
                    quantity = quantity)))
            sharedPreferences.edit().putStringSet(name, stockOrders).apply()
        } else {
            sharedPreferences.edit()
                    .putStringSet(name,
                            setOf(Json.encodeToString(StockOrder(orderAction = "Sell", currency = currency.toString(),
                                    dateInMillis = currentTimeInMillis, name = name, pricePerStock = pricePerStock,
                                    quantity = quantity))))
                    .apply()
        }
    }

    private fun Double.roundTo(n: Int): Double {
        return String.format(Locale.ENGLISH, "%.${n}f", this).toDouble()
    }

    override fun toString(): String {
        return "Sell: $quantity of $name at price ${pricePerStock.roundTo(3)} $currency"
    }

}
