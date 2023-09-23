package com.sundbybergsit.cromfortune.algorithm.core

import com.sundbybergsit.cromfortune.algorithm.api.StockOrderCommand
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderApi
import com.sundbybergsit.cromfortune.domain.util.roundTo
import java.util.Currency

class SellStockCommand(
    private val currentTimeInMillis: Long,
    val currency: Currency,
    val name: String,
    val pricePerStock: Double,
    val quantity: Int,
    val commissionFee: Double
) : StockOrderCommand {

    override fun quantity(): Int = quantity

    override fun stockSymbol(): String = name

    override fun currency(): Currency = currency

    override fun commissionFee(): Double = commissionFee

    override fun price(): Double = pricePerStock

    override fun execute(repository: StockOrderApi) {
        if (repository.count(name) > 0) {
            val stockOrders: MutableSet<StockOrder> = repository.list(name).toMutableSet()
            stockOrders.add(
                StockOrder(
                    orderAction = "Sell", currency = currency.toString(),
                    dateInMillis = currentTimeInMillis, name = name, pricePerStock = pricePerStock,
                    quantity = quantity
                )
            )
            repository.putAll(name, stockOrders)
        } else {
            repository.putReplacingAll(
                name, StockOrder(
                    orderAction = "Sell", currency = currency.toString(),
                    dateInMillis = currentTimeInMillis, name = name, pricePerStock = pricePerStock,
                    commissionFee = commissionFee, quantity = quantity
                )
            )
        }
    }

    override fun toString(): String {
        return "Sell: $quantity of $name at price ${pricePerStock.roundTo(3)} $currency with commission fee $commissionFee SEK"
    }

}
