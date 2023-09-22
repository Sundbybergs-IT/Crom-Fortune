package com.sundbybergsit.cromfortune.domain

interface StockEventRepository {

    fun listOfStockNames(): Iterable<String>

    fun isEmpty(): Boolean

    fun list(stockName: String): Set<StockEvent>

    fun remove(stockName: String)

}
