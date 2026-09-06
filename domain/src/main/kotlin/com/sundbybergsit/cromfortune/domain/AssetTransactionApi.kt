package com.sundbybergsit.cromfortune.domain

import java.math.BigDecimal

interface AssetTransactionApi {
    fun currentQuantity(assetId: String): BigDecimal
    fun assetIds(): Set<String>
    fun isEmpty(): Boolean
    fun list(assetId: String): Set<AssetTransaction>
    fun putAll(assetId: String, transactions: Set<AssetTransaction>)
    fun putReplacingAll(assetId: String, transaction: AssetTransaction)
    fun remove(assetId: String)
    fun remove(transaction: AssetTransaction)
}
