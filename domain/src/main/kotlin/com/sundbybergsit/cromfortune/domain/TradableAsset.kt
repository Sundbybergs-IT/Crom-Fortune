package com.sundbybergsit.cromfortune.domain

import java.util.Currency

data class TradableAsset(
    val id: String,
    val symbol: String,
    val displayName: String,
    val type: AssetType,
    val quoteCurrency: Currency,
    val marketDataSymbol: String,
    val quantityScale: Int
) {

    init {
        require(id.isNotBlank()) { "Asset ID must not be blank" }
        require(symbol.isNotBlank()) { "Asset symbol must not be blank" }
        require(displayName.isNotBlank()) { "Asset display name must not be blank" }
        require(marketDataSymbol.isNotBlank()) { "Asset market-data symbol must not be blank" }
        require(quantityScale >= 0) { "Asset quantity scale must not be negative" }
    }
}

