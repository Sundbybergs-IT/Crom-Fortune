package com.sundbybergsit.cromfortune.domain

import java.util.Currency

data class AssetPrice(
    val assetId: String,
    val currency: Currency,
    val price: Double
) {

    init {
        require(assetId.isNotBlank()) { "Asset ID must not be blank" }
        require(price >= 0.0 && price.isFinite()) { "Asset price must be finite and non-negative" }
    }
}

