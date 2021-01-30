package com.sundbybergsit.cromfortune.ui.home

import android.content.Context
import com.sundbybergsit.cromfortune.roundTo
import com.sundbybergsit.cromfortune.stocks.StockOrderRepositoryImpl
import java.util.*

class SellStockCommand(private val context: Context, private val currentTimeInMillis: Long,
                       private val currency: Currency, private val name: String, private val pricePerStock: Double,
                       private val quantity: Int, private val commissionFee: Double) : Command {

    override fun execute() {
        val stockOrderRepository = StockOrderRepositoryImpl(context)
        if (stockOrderRepository.count(name) > 0) {
            val stockOrders: MutableSet<StockOrder> = stockOrderRepository.list(name).toMutableSet()
            stockOrders.add(StockOrder(orderAction = "Sell", currency = currency.toString(),
                    dateInMillis = currentTimeInMillis, name = name, pricePerStock = pricePerStock,
                    quantity = quantity))
            stockOrderRepository.putAll(name, stockOrders)
        } else {
            stockOrderRepository.put(name, StockOrder(orderAction = "Sell", currency = currency.toString(),
                                    dateInMillis = currentTimeInMillis, name = name, pricePerStock = pricePerStock,
                                    commissionFee = commissionFee, quantity = quantity))
        }
    }

    override fun toString(): String {
        return "Sell: $quantity of $name at price ${pricePerStock.roundTo(3)} $currency"
    }

}
