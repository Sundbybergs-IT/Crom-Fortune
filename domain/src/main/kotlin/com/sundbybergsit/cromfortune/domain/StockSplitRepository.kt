package com.sundbybergsit.cromfortune.domain

interface StockSplitRepository {

    fun list(stockName: String): Set<StockSplit>

    fun putAll(stockName: String, stockSplits: Set<StockSplit>)

    fun putReplacingAll(stockName: String, stockSplit: StockSplit)

    fun remove(stockName: String)

    fun remove(stockSplit: StockSplit)

}
