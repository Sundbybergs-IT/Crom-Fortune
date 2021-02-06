package com.sundbybergsit.cromfortune.ui.home

interface StockPriceListener {

    fun getStockPrice(stockSymbol: String): StockPrice

}

