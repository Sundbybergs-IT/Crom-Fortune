package com.sundbybergsit.cromfortune.algorithm.cromfortunev1

import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderApi

class StubbedStockOrderApi(private val stockOrders: MutableSet<StockOrder> = mutableSetOf()) : StockOrderApi {

    override fun count(stockSymbol: String): Int =
        stockOrders.count { stockOrder -> stockOrder.name == stockSymbol }

    override fun countAll(): Int = stockOrders.size

    override fun listOfStockNames(): Iterable<String> = stockOrders.map { stockOrder -> stockOrder.name }

    override fun isEmpty(): Boolean = stockOrders.isEmpty()

    override fun list(stockSymbol: String): Set<StockOrder> =
        stockOrders.filter { stockOrder -> stockOrder.name == stockSymbol }.toSet()

    override fun putAll(stockSymbol: String, stockOrders: Set<StockOrder>) {
        this.stockOrders.addAll(stockOrders)
    }

    override fun putReplacingAll(stockSymbol: String, stockOrder: StockOrder) {
        this.stockOrders.addAll(stockOrders)
    }

    override fun remove(stockSymbol: String) {
        this.stockOrders.removeIf { stockOrder -> stockOrder.name == stockSymbol }
    }

    override fun remove(stockOrder: StockOrder) {
        this.stockOrders.remove(stockOrder)
    }

}
