package com.sundbybergsit.cromfortune.main.ui.home

import com.sundbybergsit.cromfortune.algorithm.api.RecommendationAlgorithm
import com.sundbybergsit.cromfortune.algorithm.core.BuyStockCommand
import com.sundbybergsit.cromfortune.algorithm.core.SellStockCommand
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderAggregate
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.notifications.NotificationMessage
import com.sundbybergsit.cromfortune.main.AssetDataRetrievalCoroutineWorker

fun StockOrderAggregate.applyStockOrderForRecommendedEvent(
    eventToConsider: StockEvent,
    existingEvents: List<StockEvent>,
    recommendationAlgorithm: RecommendationAlgorithm,
    cromCashWallet: SimulatedCashWallet,
    userSimulatedCashWallet: SimulatedCashWallet
) : StockEvent? {
    val stockOrder = checkNotNull(eventToConsider.stockOrder)
    if (stockOrder.orderAction == "Buy") {
        val outsideCapital = userSimulatedCashWallet.buyWithTopUp(
            quantity = stockOrder.quantity,
            pricePerStock = stockOrder.pricePerStock,
            rateInSek = rateInSek,
            commissionFeeSek = stockOrder.commissionFee
        )
        cromCashWallet.fund(amountSek = outsideCapital)
    } else {
        userSimulatedCashWallet.sell(
            quantity = stockOrder.quantity,
            pricePerStock = stockOrder.pricePerStock,
            rateInSek = rateInSek,
            commissionFeeSek = stockOrder.commissionFee
        )
    }
    return applyRecommendationAt(
        pricePerStock = stockOrder.pricePerStock,
        dateInMillis = eventToConsider.dateInMillis,
        existingEvents = existingEvents,
        recommendationAlgorithm = recommendationAlgorithm,
        cromCashWallet = cromCashWallet
    )
}

fun StockOrderAggregate.applyNotificationForRecommendedEvent(
    notification: NotificationMessage,
    existingEvents: List<StockEvent>,
    recommendationAlgorithm: RecommendationAlgorithm,
    cromCashWallet: SimulatedCashWallet
): StockEvent? {
    val pricePerStock = requireNotNull(notification.pricePerStock)
    return applyRecommendationAt(
        pricePerStock = pricePerStock,
        dateInMillis = notification.dateInMillis,
        existingEvents = existingEvents,
        recommendationAlgorithm = recommendationAlgorithm,
        cromCashWallet = cromCashWallet
    )
}

private fun StockOrderAggregate.applyRecommendationAt(
    pricePerStock: Double,
    dateInMillis: Long,
    existingEvents: List<StockEvent>,
    recommendationAlgorithm: RecommendationAlgorithm,
    cromCashWallet: SimulatedCashWallet
): StockEvent? {
    val recommendation = recommendationAlgorithm
        .getRecommendation(
            StockPrice(
                stockSymbol = stockSymbol,
                currency = this.currency,
                price = pricePerStock
            ),
            currencyRateInSek = this.rateInSek,
            commissionFee = AssetDataRetrievalCoroutineWorker.COMMISSION_FEE,
            stockEvents = existingEvents.toSet(),
            timeInMillis = dateInMillis
        )
    when (recommendation?.command) {
        is BuyStockCommand -> {
            val quantity = cromCashWallet.maximumAffordableQuantity(
                pricePerStock = pricePerStock,
                rateInSek = rateInSek,
                commissionFeeSek = AssetDataRetrievalCoroutineWorker.COMMISSION_FEE
            ).coerceAtMost(recommendation.command.quantity())
            if (quantity == 0) return null
            val buyOrder = StockOrder(
                orderAction = "Buy",
                currency = this.currency.toString(),
                dateInMillis = dateInMillis,
                name = stockSymbol,
                pricePerStock = pricePerStock,
                commissionFee = AssetDataRetrievalCoroutineWorker.COMMISSION_FEE,
                quantity = quantity
            )
            cromCashWallet.buy(
                quantity = quantity,
                pricePerStock = pricePerStock,
                rateInSek = rateInSek,
                commissionFeeSek = AssetDataRetrievalCoroutineWorker.COMMISSION_FEE
            )
            return StockEvent(buyOrder, null, dateInMillis)
        }
        is SellStockCommand -> {
            val sellOrder = StockOrder(
                orderAction = "Sell",
                currency = this.currency.toString(),
                dateInMillis = dateInMillis,
                name = stockSymbol,
                pricePerStock = pricePerStock,
                commissionFee = AssetDataRetrievalCoroutineWorker.COMMISSION_FEE,
                quantity = recommendation.command.quantity()
            )
            cromCashWallet.sell(
                quantity = recommendation.command.quantity(),
                pricePerStock = pricePerStock,
                rateInSek = rateInSek,
                commissionFeeSek = AssetDataRetrievalCoroutineWorker.COMMISSION_FEE
            )
            return StockEvent(sellOrder, null, dateInMillis)
        }
        else -> {
            // Do nothing
            return null
        }
    }
}
