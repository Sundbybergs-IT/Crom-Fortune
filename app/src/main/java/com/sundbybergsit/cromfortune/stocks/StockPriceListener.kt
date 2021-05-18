package com.sundbybergsit.cromfortune.stocks

interface StockPriceListener {

    fun getStockPrice(stockSymbol: String): com.sundbybergsit.cromfortune.domain.StockPrice

}

