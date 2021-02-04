package com.sundbybergsit.cromfortune.stocks

import com.sundbybergsit.cromfortune.ui.home.StockOrder

interface StockOrderRepository {

    fun count(stockName: String): Int

    fun listOfStockNames(): Iterable<String>

    fun isEmpty(): Boolean

    fun list(stockName: String): Set<StockOrder>

    fun putAll(stockName: String, stockOrders: Set<StockOrder>)

    fun put(stockName: String, stockOrder: StockOrder)

    fun remove(stockName: String)

    fun remove(stockOrder: StockOrder)

}
