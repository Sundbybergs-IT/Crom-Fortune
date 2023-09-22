package com.sundbybergsit.cromfortune.main.ui.home

import com.sundbybergsit.cromfortune.algorithm.BuyStockCommand
import com.sundbybergsit.cromfortune.algorithm.SellStockCommand
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderAggregate
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.main.StockDataRetrievalCoroutineWorker
import com.sundbybergsit.cromfortune.main.crom.CromFortuneV1RecommendationAlgorithm

fun StockOrderAggregate.applyStockOrderForRecommendedEvent(
    eventToConsider: StockEvent,
    existingEvents: List<StockEvent>,
    recommendationAlgorithm: CromFortuneV1RecommendationAlgorithm
) : StockEvent? {
    val stockOrder = checkNotNull(eventToConsider.stockOrder)
    val recommendation = recommendationAlgorithm
        .getRecommendation(
            StockPrice(
                stockOrder.name,
                this.currency, stockOrder.pricePerStock
            ),
            this.rateInSek, StockDataRetrievalCoroutineWorker.COMMISSION_FEE,
            existingEvents.toSet(),
            eventToConsider.dateInMillis
        )
    when (recommendation?.command) {
        is BuyStockCommand -> {
            val buyOrder = StockOrder(
                "Buy", this.currency.toString(),
                eventToConsider.dateInMillis, stockOrder.name, stockOrder.pricePerStock,
                StockDataRetrievalCoroutineWorker.COMMISSION_FEE, recommendation.command.quantity()
            )
            return StockEvent(buyOrder, null, eventToConsider.dateInMillis)
        }
        is SellStockCommand -> {
            val sellOrder = StockOrder(
                "Sell", this.currency.toString(),
                eventToConsider.dateInMillis, stockOrder.name, stockOrder.pricePerStock,
                StockDataRetrievalCoroutineWorker.COMMISSION_FEE, recommendation.command.quantity()
            )
            return StockEvent(sellOrder, null, eventToConsider.dateInMillis)
        }
        else -> {
            // Do nothing
            return null
        }
    }
}
