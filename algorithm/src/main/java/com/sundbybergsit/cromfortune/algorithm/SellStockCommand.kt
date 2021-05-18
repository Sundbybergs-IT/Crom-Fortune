package com.sundbybergsit.cromfortune.algorithm

import android.content.Context
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderRepository
import com.sundbybergsit.cromfortune.roundTo
import java.util.*

class SellStockCommand(private val context: Context, private val currentTimeInMillis: Long,
                       val currency: Currency, val name: String, val pricePerStock: Double,
                       val quantity: Int, val commissionFee: Double) : StockOrderCommand {

    override fun quantity(): Int = quantity

    override fun stockSymbol(): String = name

    override fun currency(): Currency = currency

    override fun commissionFee(): Double = commissionFee

    override fun price(): Double = pricePerStock

    override fun execute(repository: StockOrderRepository) {
        if (repository.count(name) > 0) {
            val stockOrders: MutableSet<StockOrder> = repository.list(name).toMutableSet()
            stockOrders.add(StockOrder(orderAction = "Sell", currency = currency.toString(),
                    dateInMillis = currentTimeInMillis, name = name, pricePerStock = pricePerStock,
                    quantity = quantity))
            repository.putAll(name, stockOrders)
        } else {
            repository.putReplacingAll(name, StockOrder(orderAction = "Sell", currency = currency.toString(),
                                    dateInMillis = currentTimeInMillis, name = name, pricePerStock = pricePerStock,
                                    commissionFee = commissionFee, quantity = quantity))
        }
    }

    override fun toString(): String {
        return "Sell: $quantity of $name at price ${pricePerStock.roundTo(3)} $currency with commission fee $commissionFee SEK"
    }

}
