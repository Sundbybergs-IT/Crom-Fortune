package com.sundbybergsit.cromfortune.domain

interface StockOrderApi {

    fun count(stockSymbol: String): Int

    fun countAll(): Int

    fun listOfStockNames(): Iterable<String>

    fun isEmpty(): Boolean

    fun list(stockSymbol: String): Set<StockOrder>

    fun putAll(stockSymbol: String, stockOrders: Set<StockOrder>)

    fun putReplacingAll(stockSymbol: String, stockOrder: StockOrder)

    fun remove(stockSymbol: String)

    fun remove(stockOrder: StockOrder)

}
