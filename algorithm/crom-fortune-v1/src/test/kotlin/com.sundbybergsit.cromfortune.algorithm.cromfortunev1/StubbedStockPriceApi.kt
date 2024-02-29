package com.sundbybergsit.cromfortune.algorithm.cromfortunev1

import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.StockPriceApi

class StubbedStockPriceApi(private val stockPrices: MutableSet<StockPrice> = mutableSetOf()) : StockPriceApi {

    override fun put(stockPrice: Set<StockPrice>) {
        stockPrices.addAll(stockPrice)
    }

    override fun getStockPrice(stockSymbol: String): StockPrice? {
        return stockPrices.find { stockPrice -> stockPrice.stockSymbol == stockSymbol }
    }

}
