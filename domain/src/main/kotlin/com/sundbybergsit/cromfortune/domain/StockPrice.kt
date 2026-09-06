package com.sundbybergsit.cromfortune.domain

import java.util.Currency

data class StockPrice(val stockSymbol: String, val currency: Currency, val price: Double) {

    companion object {

        @Deprecated("Use AssetCatalog.assets quote currencies")
        val CURRENCIES: Array<String>
            get() = AssetCatalog.assets.map { asset -> asset.quoteCurrency.currencyCode }.toSortedSet().toTypedArray()

        @Deprecated("Use AssetCatalog.stocks")
        val SYMBOLS: Array<Triple<String, String, String>>
            get() = AssetCatalog.stocks.map { asset ->
                Triple(asset.symbol, asset.displayName, asset.quoteCurrency.currencyCode)
            }.toTypedArray()
    }
}
