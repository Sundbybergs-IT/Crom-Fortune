package com.sundbybergsit.cromfortune.domain

import java.math.BigDecimal
import java.util.Currency

data class AssetPrice(
    val assetId: String,
    val currency: Currency,
    val price: BigDecimal
) {

    constructor(assetId: String, currency: Currency, price: Double) :
        this(assetId, currency, price.toBigDecimal())

    init {
        require(assetId.isNotBlank()) { "Asset ID must not be blank" }
        require(price >= BigDecimal.ZERO) { "Asset price must be non-negative" }
    }
}
