package com.sundbybergsit.cromfortune.ui.home

import com.sundbybergsit.cromfortune.stocks.StockPrice

interface StockPriceListener {

    fun getStockPrice(stockSymbol: String): StockPrice

}

