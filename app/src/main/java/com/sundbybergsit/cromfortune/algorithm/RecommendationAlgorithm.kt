package com.sundbybergsit.cromfortune.algorithm

import com.sundbybergsit.cromfortune.stocks.StockOrder
import com.sundbybergsit.cromfortune.stocks.StockPrice

abstract class RecommendationAlgorithm {

    abstract fun getRecommendation(
            stockPrice: StockPrice, currencyRateInSek: Double, commissionFee: Double, previousOrders: Set<StockOrder>,
            timeInMillis: Long,
    ): Recommendation?

}
