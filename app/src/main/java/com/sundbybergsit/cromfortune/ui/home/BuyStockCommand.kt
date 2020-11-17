package com.sundbybergsit.cromfortune.ui.home

import android.content.Context
import com.sundbybergsit.cromfortune.stocks.StocksPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

class BuyStockCommand(private val context: Context, private val currentTimeInMillis: Long,
                      private val currency: Currency, private val name: String, private val pricePerStock: Double,
                      private val quantity: Int, private val commissionFee: Double)
    : Command {

    override fun execute() {
        val sharedPreferences = context.getSharedPreferences(StocksPreferences.PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (sharedPreferences.contains(name)) {
            val stockOrders: MutableSet<String> = sharedPreferences.getStringSet(name, null) as MutableSet<String>
            stockOrders.add(Json.encodeToString(StockOrder("Buy", currency.toString(), currentTimeInMillis,
                    name, pricePerStock, commissionFee, quantity)))
            sharedPreferences.edit().putStringSet(name, stockOrders).apply()
        } else {
            sharedPreferences.edit()
                    .putStringSet(name,
                            setOf(Json.encodeToString(StockOrder("Buy", currency.toString(),
                                    currentTimeInMillis, name, pricePerStock, commissionFee, quantity))))
                    .apply()
        }
    }

    override fun toString(): String {
        return "Buy: $quantity of $name at price $pricePerStock $currency with commission fee $commissionFee $currency"
    }

}
