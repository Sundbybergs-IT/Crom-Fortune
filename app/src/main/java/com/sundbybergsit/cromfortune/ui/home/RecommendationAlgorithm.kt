package com.sundbybergsit.cromfortune.ui.home

abstract class RecommendationAlgorithm {

    abstract suspend fun getRecommendation(
            stockPrice: StockPrice, commissionFee: Double, previousOrders: Set<StockOrder>,
    ): Recommendation?

}
