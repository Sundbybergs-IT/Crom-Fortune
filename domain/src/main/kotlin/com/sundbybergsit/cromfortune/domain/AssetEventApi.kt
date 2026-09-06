package com.sundbybergsit.cromfortune.domain

import java.math.BigDecimal

interface AssetEventApi {
    fun currentQuantity(assetId: String): BigDecimal
    fun assetIds(): Set<String>
    fun isEmpty(): Boolean
    fun list(assetId: String): Set<AssetEvent>
    fun remove(assetId: String)
}
