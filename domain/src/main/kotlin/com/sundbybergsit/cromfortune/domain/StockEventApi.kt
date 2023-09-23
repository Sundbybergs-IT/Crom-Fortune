package com.sundbybergsit.cromfortune.domain

interface StockEventApi {

    fun listOfStockNames(): Iterable<String>

    fun isEmpty(): Boolean

    fun list(stockName: String): Set<StockEvent>

    fun remove(stockName: String)

}
