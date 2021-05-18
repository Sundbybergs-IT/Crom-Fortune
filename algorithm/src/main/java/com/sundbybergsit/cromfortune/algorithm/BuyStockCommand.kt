package com.sundbybergsit.cromfortune.algorithm

import android.content.Context
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderRepository
import java.util.*

class BuyStockCommand(private val context: Context, private val currentTimeInMillis: Long,
                      val currency: Currency, val name: String, val pricePerStock: Double,
                      val quantity: Int, val commissionFee: Double)
    : StockOrderCommand {

    override fun quantity(): Int = quantity

    override fun stockSymbol(): String = name

    override fun currency(): Currency = currency

    override fun commissionFee(): Double = commissionFee

    override fun price(): Double = pricePerStock

    override fun execute(repository : StockOrderRepository) {
        if (repository.count(name) > 0) {
            val stockOrders: MutableSet<StockOrder> = repository.list(name).toMutableSet()
            stockOrders.add(StockOrder("Buy", currency.toString(), currentTimeInMillis,
                    name, pricePerStock, commissionFee, quantity))
            repository.putAll(name, stockOrders)
        } else {
            repository.putReplacingAll(name, StockOrder(orderAction = "Buy", currency = currency.toString(),
                                    dateInMillis = currentTimeInMillis, name = name, pricePerStock = pricePerStock,
                                    commissionFee = commissionFee, quantity = quantity))
        }
    }

    override fun toString(): String {
        return "Buy: $quantity of $name at price $pricePerStock $currency with commission fee $commissionFee SEK"
    }

}
