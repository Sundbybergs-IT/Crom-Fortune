package com.sundbybergsit.cromfortune.main.crom

import com.sundbybergsit.cromfortune.algorithm.api.RecommendationAlgorithm
import com.sundbybergsit.cromfortune.algorithm.core.ConformanceScore
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.main.currencies.CurrencyRateRepository

abstract class AlgorithmConformanceScoreCalculator {

    abstract suspend fun getScore(
        recommendationAlgorithm: RecommendationAlgorithm,
        stockEvents: Set<StockEvent>,
        currencyRateRepository: CurrencyRateRepository
    ): ConformanceScore

}
