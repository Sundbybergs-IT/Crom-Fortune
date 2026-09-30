package com.sundbybergsit.cromfortune.domain

import java.util.Currency

enum class AssetStatus {
    ACTIVE,
    DELISTED
}

data class TradableAsset(
    val id: String,
    val symbol: String,
    val displayName: String,
    val type: AssetType,
    val quoteCurrency: Currency,
    val marketDataSymbol: String,
    val quantityScale: Int,
    val status: AssetStatus = AssetStatus.ACTIVE
) {

    val isActive: Boolean get() = status == AssetStatus.ACTIVE

    init {
        require(id.isNotBlank()) { "Asset ID must not be blank" }
        require(symbol.isNotBlank()) { "Asset symbol must not be blank" }
        require(displayName.isNotBlank()) { "Asset display name must not be blank" }
        require(marketDataSymbol.isNotBlank()) { "Asset market-data symbol must not be blank" }
        require(quantityScale >= 0) { "Asset quantity scale must not be negative" }
    }
}
