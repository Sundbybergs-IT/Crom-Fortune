package com.sundbybergsit.cromfortune.algorithm

import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockPrice

abstract class RecommendationAlgorithm {

    abstract fun getRecommendation(
        stockPrice: StockPrice, currencyRateInSek: Double, commissionFee: Double, previousOrders: Set<StockOrder>,
        timeInMillis: Long,
    ): Recommendation?

}
