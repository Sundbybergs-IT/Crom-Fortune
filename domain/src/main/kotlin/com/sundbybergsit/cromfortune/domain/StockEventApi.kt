package com.sundbybergsit.cromfortune.domain

interface StockEventApi {

    fun countCurrent(stockName: String) : Int

    fun listOfStockNames(): Iterable<String>

    fun isEmpty(): Boolean

    fun list(stockName: String): List<StockEvent>

    fun remove(stockName: String)

}
