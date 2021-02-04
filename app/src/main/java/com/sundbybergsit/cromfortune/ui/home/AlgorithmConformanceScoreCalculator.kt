package com.sundbybergsit.cromfortune.ui.home

abstract class AlgorithmConformanceScoreCalculator {

    abstract suspend fun getScore(recommendationAlgorithm: RecommendationAlgorithm,
                                  orders: Set<StockOrder>,
                                  currencyConversionRateProducer: CurrencyConversionRateProducer): ConformanceScore

}
