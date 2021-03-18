package com.sundbybergsit.cromfortune.algorithm

import android.content.Context
import com.sundbybergsit.cromfortune.roundTo
import com.sundbybergsit.cromfortune.stocks.StockOrder
import com.sundbybergsit.cromfortune.stocks.StockOrderRepositoryImpl
import java.util.*

class SellStockCommand(private val context: Context, private val currentTimeInMillis: Long,
                       val currency: Currency, val name: String, val pricePerStock: Double,
                       val quantity: Int, val commissionFee: Double) : StockOrderCommand {

    override fun quantity(): Int = quantity

    override fun stockSymbol(): String = name

    override fun currency(): Currency = currency

    override fun commissionFee(): Double = commissionFee

    override fun price(): Double = pricePerStock

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
        return "Sell: $quantity of $name at price ${pricePerStock.roundTo(3)} $currency with commission fee $commissionFee SEK"
    }

}
