package com.sundbybergsit.cromfortune.algorithm.core

import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderApi

class TestableStockOrderApi(private val stockOrders: MutableSet<StockOrder>) : StockOrderApi {

    override fun count(stockSymbol: String): Int = stockOrders.map { stockOrder -> stockOrder.name }.size

    override fun countAll(): Int = stockOrders.size

    override fun listOfAssetNames(): Iterable<String> = stockOrders.map { stockOrder -> stockOrder.name }

    override fun isEmpty(): Boolean  = stockOrders.isEmpty()

    override fun list(stockSymbol: String): Set<StockOrder> = stockOrders.filter { stockOrder -> stockOrder.name == stockSymbol }.toSet()

    override fun putAll(stockSymbol: String, stockOrders: Set<StockOrder>) {
        this.stockOrders.addAll(stockOrders)
    }

    override fun putReplacingAll(stockSymbol: String, stockOrder: StockOrder) {
        this.stockOrders.add(stockOrder)
    }

    override fun remove(stockSymbol: String) {
        this.stockOrders.removeIf { stockOrder -> stockOrder.name == stockSymbol }
    }

    override fun remove(stockOrder: StockOrder) {
        this.stockOrders.remove(stockOrder)
    }

}
