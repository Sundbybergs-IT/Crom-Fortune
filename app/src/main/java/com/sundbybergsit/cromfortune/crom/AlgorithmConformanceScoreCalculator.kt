package com.sundbybergsit.cromfortune.crom

import com.sundbybergsit.cromfortune.algorithm.ConformanceScore
import com.sundbybergsit.cromfortune.algorithm.RecommendationAlgorithm
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.stocks.StockOrder

abstract class AlgorithmConformanceScoreCalculator {

    abstract suspend fun getScore(recommendationAlgorithm: RecommendationAlgorithm,
                                  orders: Set<StockOrder>,
                                  currencyRateRepository : CurrencyRateRepository
    ): ConformanceScore

}
