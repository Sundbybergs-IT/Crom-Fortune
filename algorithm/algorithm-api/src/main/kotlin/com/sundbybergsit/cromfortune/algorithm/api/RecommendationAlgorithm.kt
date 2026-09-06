package com.sundbybergsit.cromfortune.algorithm.api

import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockPrice

abstract class RecommendationAlgorithm {

    abstract val supportedAssetTypes: Set<AssetType>

    fun supports(assetType: AssetType): Boolean = assetType in supportedAssetTypes

    fun supports(stockEvents: Collection<StockEvent>): Boolean = stockEvents
        .mapNotNull { stockEvent -> stockEvent.stockOrder?.assetType }
        .all(::supports)

    abstract fun getRecommendation(
        stockPrice: StockPrice, currencyRateInSek: Double, commissionFee: Double, stockEvents: Set<StockEvent>,
        timeInMillis: Long,
    ): Recommendation?

}
