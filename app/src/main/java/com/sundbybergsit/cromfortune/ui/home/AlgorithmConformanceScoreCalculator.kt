package com.sundbybergsit.cromfortune.ui.home

import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository

abstract class AlgorithmConformanceScoreCalculator {

    abstract suspend fun getScore(recommendationAlgorithm: RecommendationAlgorithm,
                                  orders: Set<StockOrder>,
                                  currencyRateRepository : CurrencyRateRepository
    ): ConformanceScore

}
