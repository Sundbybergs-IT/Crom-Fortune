package com.sundbybergsit.cromfortune.domain

data class AssetEvent(
    val assetId: String,
    val assetType: AssetType,
    val transaction: AssetTransaction? = null,
    val stockSplit: StockSplit? = null,
    val dateInMillis: Long
) {
    init {
        require((transaction == null) != (stockSplit == null)) { "Exactly one event value must be set" }
        require(transaction == null || transaction.assetId == assetId) { "Transaction asset ID must match event" }
        require(transaction == null || transaction.assetType == assetType) { "Transaction asset type must match event" }
        require(stockSplit == null || assetType == AssetType.STOCK) { "Splits are only supported for stocks" }
    }
}

val assetEventChronologicalComparator: Comparator<AssetEvent> =
    compareBy(AssetEvent::dateInMillis).thenBy { event ->
        when (event.transaction?.action) {
            TransactionAction.BUY -> 0
            null -> 1
            TransactionAction.SELL -> 2
        }
    }
