package com.sundbybergsit.cromfortune.algorithm.core

import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderApi

class TestableStockOrderApi(private val stockOrders: MutableSet<StockOrder>) : StockOrderApi {

    override fun count(stockName: String): Int = stockOrders.map { stockOrder -> stockOrder.name }.size

    override fun countAll(): Int = stockOrders.size

    override fun listOfStockNames(): Iterable<String> = stockOrders.map { stockOrder -> stockOrder.name }

    override fun isEmpty(): Boolean  = stockOrders.isEmpty()

    override fun list(stockName: String): Set<StockOrder> = stockOrders.filter { stockOrder -> stockOrder.name == stockName }.toSet()

    override fun putAll(stockName: String, stockOrders: Set<StockOrder>) {
        this.stockOrders.addAll(stockOrders)
    }

    override fun putReplacingAll(stockName: String, stockOrder: StockOrder) {
        this.stockOrders.add(stockOrder)
    }

    override fun remove(stockName: String) {
        this.stockOrders.removeIf { stockOrder -> stockOrder.name == stockName }
    }

    override fun remove(stockOrder: StockOrder) {
        this.stockOrders.remove(stockOrder)
    }

}
