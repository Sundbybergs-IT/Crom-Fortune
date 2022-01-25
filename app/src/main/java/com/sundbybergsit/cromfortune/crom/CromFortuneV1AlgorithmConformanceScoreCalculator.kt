package com.sundbybergsit.cromfortune.crom

import android.util.Log
import com.sundbybergsit.cromfortune.algorithm.BuyStockCommand
import com.sundbybergsit.cromfortune.algorithm.ConformanceScore
import com.sundbybergsit.cromfortune.algorithm.RecommendationAlgorithm
import com.sundbybergsit.cromfortune.algorithm.SellStockCommand
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderAggregate
import com.sundbybergsit.cromfortune.domain.StockPrice
import java.util.*

class CromFortuneV1AlgorithmConformanceScoreCalculator : AlgorithmConformanceScoreCalculator() {

    companion object {

        private const val TAG = "CromFortuneV1AlgorithmC"

    }

    override suspend fun getScore(
        recommendationAlgorithm: RecommendationAlgorithm,
        stockEvents: Set<StockEvent>,
        currencyRateRepository: CurrencyRateRepository,
    ): ConformanceScore {
        var correctDecision = 0
        val stockOrders: MutableList<StockOrder> = stockEvents
            .filter { stockEvent -> stockEvent.stockOrder != null }
            .map { stockEvent -> stockEvent.stockOrder!! }.toMutableList()
        val stockNames = stockOrders.map { order -> order.name }.toSet()

        // FIXME: recommend in groups of stocks

        for (stockName in stockNames) {
            var firstItemAdded = false
            val stockOrdersForStock: MutableList<StockOrder> = stockEvents
                .filter { stockEvent -> stockEvent.stockOrder != null && stockEvent.stockOrder!!.name == stockName }
                .map { stockEvent -> stockEvent.stockOrder!! }
                .sortedBy { stockOrder -> stockOrder.dateInMillis }.toMutableList()
            val firstStockOrderForStock = stockOrdersForStock.first()
            val currencyRateInSek = (currencyRateRepository.currencyRates.value as
                    CurrencyRateRepository.ViewState.VALUES).currencyRates.find { currencyRate -> currencyRate.iso4217CurrencySymbol == firstStockOrderForStock.currency }!!.rateInSek
            val stockOrderAggregate = StockOrderAggregate(
                rateInSek = currencyRateInSek,
                displayName = "FIXME",
                stockSymbol = firstStockOrderForStock.name,
                currency = Currency.getInstance(firstStockOrderForStock.currency)
            )
            val allSortedStockEvents = stockEvents.filter { stockEvent ->
                (stockEvent.stockOrder != null && stockEvent.stockOrder!!.name == stockOrderAggregate.stockSymbol ||
                        (stockEvent.stockSplit != null && stockEvent.stockSplit!!.name == stockOrderAggregate.stockSymbol))
            }.sortedBy { stockEvent -> stockEvent.dateInMillis }
            allSortedStockEvents.forEachIndexed { index, stockEvent ->
                if (!firstItemAdded) {
                    when {
                        stockEvent.stockOrder == null -> {
                            // Ignore other types of events before the first purchase order
                        }
                        stockEvent.stockOrder!!.orderAction != "Buy" -> {
                            throw IllegalStateException("First order must be a buy order!")
                        }
                        else -> {
                            correctDecision += 1
                            firstItemAdded = true
                            stockOrderAggregate.aggregate(stockEvent)
                        }
                    }
                } else if (stockEvent.stockSplit != null) {
                    stockOrderAggregate.aggregate(stockEvent)
                } else if (stockEvent.stockOrder != null) {
                    stockOrderAggregate.aggregate(stockEvent)
                    val stockOrder = stockEvent.stockOrder!!
                    val currencyRate = (currencyRateRepository.currencyRates.value as
                            CurrencyRateRepository.ViewState.VALUES).currencyRates
                        .find { currencyRate -> currencyRate.iso4217CurrencySymbol == stockOrder.currency }!!
                    val currencyRateInSek = currencyRate.rateInSek
                    val recommendation = recommendationAlgorithm.getRecommendation(
                        stockPrice = StockPrice(
                            stockSymbol = stockOrder.name,
                            currency = Currency.getInstance(stockOrder.currency),
                            price = stockOrder.pricePerStock
                        ),
                        currencyRateInSek = currencyRateInSek,
                        commissionFee = stockOrder.commissionFee,
                        allSortedStockEvents.subList(0, index).toSet(),
                        stockOrder.dateInMillis
                    )
                    if (stockOrder.orderAction == "Buy") {
                        if (recommendation != null && recommendation.command is BuyStockCommand) {
                            correctDecision += 1
                        } else {
                            Log.v(TAG, "Bad decision.")
                        }
                    } else {
                        if (recommendation != null && recommendation.command is SellStockCommand) {
                            correctDecision += 1
                        } else {
                            Log.v(TAG, "Bad decision.")
                        }
                    }
                }
            }
        }
        return when {
            stockOrders.size <= 1 -> {
                ConformanceScore(100)
            }
            correctDecision == 0 -> {
                ConformanceScore(0)
            }
            else -> {
                ConformanceScore(100 * correctDecision / stockOrders.size)
            }
        }
    }

}
