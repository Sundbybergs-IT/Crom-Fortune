package com.sundbybergsit.cromfortune.main

import yahoofinance.get
import yahoofinance.getFxHax

interface StockMarketDataClient {

    fun getRateInSek(currency: String): Double

    fun getStockPrices(symbols: Array<String>): Map<String, Double>
}

object YahooStockMarketDataClient : StockMarketDataClient {

    override fun getRateInSek(currency: String): Double =
        getFxHax("${currency}SEK=X")?.price?.toDouble() ?: 1.0

    override fun getStockPrices(symbols: Array<String>): Map<String, Double> =
        get(symbols).mapNotNull { (symbol, stock) ->
            stock.quote?.price?.toDouble()?.let { price -> symbol to price }
        }.toMap()
}
