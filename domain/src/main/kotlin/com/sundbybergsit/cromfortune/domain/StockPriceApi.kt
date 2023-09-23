package com.sundbybergsit.cromfortune.domain

interface StockPriceApi {

    fun put(stockPrice: Set<StockPrice>)
    fun getStockPrice(stockSymbol: String): StockPrice

}

