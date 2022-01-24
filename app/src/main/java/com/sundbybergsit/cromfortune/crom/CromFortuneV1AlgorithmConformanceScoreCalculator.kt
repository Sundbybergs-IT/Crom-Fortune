package com.sundbybergsit.cromfortune.crom

import android.util.Log
import com.sundbybergsit.cromfortune.algorithm.BuyStockCommand
import com.sundbybergsit.cromfortune.algorithm.ConformanceScore
import com.sundbybergsit.cromfortune.algorithm.RecommendationAlgorithm
import com.sundbybergsit.cromfortune.algorithm.SellStockCommand
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockOrder
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

        val listOfListOfStockEvents: MutableList<List<StockEvent>> = mutableListOf<List<StockEvent>>().apply {
            for (stockName in stockNames) {
                add(stockEvents.sortedBy { it.dateInMillis }.filter { event ->
                    (event.stockOrder != null && event.stockOrder!!.name == stockName) ||
                            (event.stockSplit != null && event.stockSplit!!.name == stockName)
                })
            }
        }
        var firstItemAdded = false
        for (listOfStockEvents in listOfListOfStockEvents) {
            listOfStockEvents.forEachIndexed { index, event ->
                val stockOrder = event.stockOrder
                if (!firstItemAdded) {
                    when {
                        stockOrder == null -> {
                            // Ignore other types of events before the first purchase order
                        }
                        stockOrder.orderAction != "Buy" -> {
                            throw IllegalStateException("First order must be a buy order!")
                        }
                        else -> {
                            correctDecision += 1
                            firstItemAdded = true
                        }
                    }
                } else if (event.stockOrder != null) {
                    val currencyRateInSek = (currencyRateRepository.currencyRates.value as
                            CurrencyRateRepository.ViewState.VALUES).currencyRates.find { currencyRate -> currencyRate.iso4217CurrencySymbol == stockOrder!!.currency }!!.rateInSek
                    val recommendation = recommendationAlgorithm.getRecommendation(
                        StockPrice(
                            stockOrder!!.name,
                            Currency.getInstance(stockOrder.currency),
                            stockOrder.pricePerStock
                        ),
                        currencyRateInSek,
                        stockOrder.commissionFee,
                        listOfStockEvents.subList(0, index).toSet(),
                        event.dateInMillis
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
                } else {
                    Log.v(TAG, "Skipping other types of events as there is nothing to decide upon.")
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
