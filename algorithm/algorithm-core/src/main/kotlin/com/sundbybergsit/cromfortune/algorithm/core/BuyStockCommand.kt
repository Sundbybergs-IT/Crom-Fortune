package com.sundbybergsit.cromfortune.algorithm.core

import com.sundbybergsit.cromfortune.algorithm.api.StockOrderCommand
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderApi
import java.util.Currency

class BuyStockCommand(
    private val currentTimeInMillis: Long, val currency: Currency,
    val name: String, val pricePerStock: Double, val quantity: Int,
    val commissionFee: Double
) : StockOrderCommand {

    override fun quantity(): Int = quantity

    override fun stockSymbol(): String = name

    override fun currency(): Currency = currency

    override fun commissionFee(): Double = commissionFee

    override fun price(): Double = pricePerStock

    override fun execute(item: StockOrderApi) {
        if (item.count(name) > 0) {
            val stockOrders: MutableSet<StockOrder> = item.list(name).toMutableSet()
            stockOrders.add(
                StockOrder(
                    "Buy", currency.toString(), currentTimeInMillis,
                    name, pricePerStock, commissionFee, quantity
                )
            )
            item.putAll(name, stockOrders)
        } else {
            item.putReplacingAll(
                name, StockOrder(
                    orderAction = "Buy", currency = currency.toString(),
                    dateInMillis = currentTimeInMillis, name = name, pricePerStock = pricePerStock,
                    commissionFee = commissionFee, quantity = quantity
                )
            )
        }
    }

    override fun toString(): String {
        return "Buy: $quantity of $name at price $pricePerStock $currency with commission fee $commissionFee SEK"
    }

}
