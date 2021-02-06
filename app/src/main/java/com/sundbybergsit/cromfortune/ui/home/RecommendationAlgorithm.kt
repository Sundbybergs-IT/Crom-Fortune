package com.sundbybergsit.cromfortune.ui.home

import com.sundbybergsit.cromfortune.stocks.StockOrder
import com.sundbybergsit.cromfortune.stocks.StockPrice

abstract class RecommendationAlgorithm {

    abstract suspend fun getRecommendation(
            stockPrice: StockPrice, currencyRateInSek: Double, commissionFee: Double, previousOrders: Set<StockOrder>,
    ): Recommendation?

}
