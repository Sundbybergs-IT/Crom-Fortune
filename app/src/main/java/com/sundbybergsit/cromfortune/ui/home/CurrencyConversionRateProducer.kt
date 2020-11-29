package com.sundbybergsit.cromfortune.ui.home

import yahoofinance.YahooFinance

open class CurrencyConversionRateProducer {

    open fun getRateInSek(stockSymbol: String) = when {
        stockSymbol.endsWith(".OL") -> {
            YahooFinance.getFx("NOKSEK=X").price.toDouble()
        }
        stockSymbol.endsWith(".ST") -> {
            1.0
        }
        else -> {
            YahooFinance.getFx("USDSEK=X").price.toDouble()
        }
    }

}
