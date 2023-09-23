package com.sundbybergsit.cromfortune.algorithm.api

import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockPrice

abstract class RecommendationAlgorithm {

    abstract fun getRecommendation(
        stockPrice: StockPrice, currencyRateInSek: Double, commissionFee: Double, stockEvents: Set<StockEvent>,
        timeInMillis: Long,
    ): Recommendation?

}
