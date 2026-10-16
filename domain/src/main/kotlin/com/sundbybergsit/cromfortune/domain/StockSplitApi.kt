package com.sundbybergsit.cromfortune.domain

interface StockSplitApi {

    fun list(stockName: String): Set<StockSplit>

    fun listAll(): Set<StockSplit> = emptySet()

    fun putAll(stockName: String, stockSplits: Set<StockSplit>)

    fun putReplacingAll(stockName: String, stockSplit: StockSplit)

    fun remove(stockName: String)

    fun remove(stockSplit: StockSplit)

    fun update(original: StockSplit, updated: StockSplit) {
        throw UnsupportedOperationException()
    }

}
