package com.sundbybergsit.cromfortune.ui.home

abstract class RecommendationAlgorithm {

    abstract suspend fun getRecommendation(stockPrice: StockPrice, commissionFee: Double,
                                           currencyConversionRateProducer: CurrencyConversionRateProducer,
                                           previousOrders: Set<StockOrder>): Recommendation?

}
