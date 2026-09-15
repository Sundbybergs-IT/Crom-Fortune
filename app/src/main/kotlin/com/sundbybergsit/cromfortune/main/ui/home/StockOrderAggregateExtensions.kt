package com.sundbybergsit.cromfortune.main.ui.home

import com.sundbybergsit.cromfortune.algorithm.api.RecommendationAlgorithm
import com.sundbybergsit.cromfortune.algorithm.core.BuyStockCommand
import com.sundbybergsit.cromfortune.algorithm.core.SellStockCommand
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderAggregate
import com.sundbybergsit.cromfortune.domain.StockPrice
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
    val recommendation = recommendationAlgorithm
        .getRecommendation(
            StockPrice(
                stockSymbol = stockOrder.name,
                currency = this.currency,
                price = stockOrder.pricePerStock
            ),
            currencyRateInSek = this.rateInSek,
            commissionFee = AssetDataRetrievalCoroutineWorker.COMMISSION_FEE,
            stockEvents = existingEvents.toSet(),
            timeInMillis = eventToConsider.dateInMillis
        )
    when (recommendation?.command) {
        is BuyStockCommand -> {
            val quantity = cromCashWallet.maximumAffordableQuantity(
                pricePerStock = stockOrder.pricePerStock,
                rateInSek = rateInSek,
                commissionFeeSek = AssetDataRetrievalCoroutineWorker.COMMISSION_FEE
            ).coerceAtMost(recommendation.command.quantity())
            if (quantity == 0) return null
            val buyOrder = StockOrder(
                orderAction = "Buy",
                currency = this.currency.toString(),
                dateInMillis = eventToConsider.dateInMillis,
                name = stockOrder.name,
                pricePerStock = stockOrder.pricePerStock,
                commissionFee = AssetDataRetrievalCoroutineWorker.COMMISSION_FEE,
                quantity = quantity
            )
            cromCashWallet.buy(
                quantity = quantity,
                pricePerStock = stockOrder.pricePerStock,
                rateInSek = rateInSek,
                commissionFeeSek = AssetDataRetrievalCoroutineWorker.COMMISSION_FEE
            )
            return StockEvent(buyOrder, null, eventToConsider.dateInMillis)
        }
        is SellStockCommand -> {
            val sellOrder = StockOrder(
                orderAction = "Sell",
                currency = this.currency.toString(),
                dateInMillis = eventToConsider.dateInMillis,
                name = stockOrder.name,
                pricePerStock = stockOrder.pricePerStock,
                commissionFee = AssetDataRetrievalCoroutineWorker.COMMISSION_FEE,
                quantity = recommendation.command.quantity()
            )
            cromCashWallet.sell(
                quantity = recommendation.command.quantity(),
                pricePerStock = stockOrder.pricePerStock,
                rateInSek = rateInSek,
                commissionFeeSek = AssetDataRetrievalCoroutineWorker.COMMISSION_FEE
            )
            return StockEvent(sellOrder, null, eventToConsider.dateInMillis)
        }
        else -> {
            // Do nothing
            return null
        }
    }
}
