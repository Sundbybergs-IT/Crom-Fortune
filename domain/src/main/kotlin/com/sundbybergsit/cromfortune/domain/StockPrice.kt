package com.sundbybergsit.cromfortune.domain

import java.util.Currency

data class StockPrice(val stockSymbol: String, val currency: Currency, val price: Double)
