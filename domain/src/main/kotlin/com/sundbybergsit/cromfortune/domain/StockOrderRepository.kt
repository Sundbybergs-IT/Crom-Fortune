package com.sundbybergsit.cromfortune.domain

interface StockOrderRepository {

    fun count(stockName: String): Int

    fun countAll(): Int

    fun listOfStockNames(): Iterable<String>

    fun isEmpty(): Boolean

    fun list(stockName: String): Set<StockOrder>

    fun putAll(stockName: String, stockOrders: Set<StockOrder>)

    fun putReplacingAll(stockName: String, stockOrder: StockOrder)

    fun remove(stockName: String)

    fun remove(stockOrder: StockOrder)

}
