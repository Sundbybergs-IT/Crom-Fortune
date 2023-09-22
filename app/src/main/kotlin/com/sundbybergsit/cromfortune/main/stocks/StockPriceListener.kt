package com.sundbybergsit.cromfortune.main.stocks

interface StockPriceListener {

    fun getStockPrice(stockSymbol: String): com.sundbybergsit.cromfortune.domain.StockPrice

}

