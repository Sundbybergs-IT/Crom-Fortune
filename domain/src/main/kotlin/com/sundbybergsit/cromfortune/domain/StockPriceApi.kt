package com.sundbybergsit.cromfortune.domain

interface StockPriceApi {

    fun getStockPrice(stockSymbol: String): StockPrice

}

