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
import com.sundbybergsit.cromfortune.domain.StockSplit
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
        for (listOfStockEvents in listOfListOfStockEvents) {
            listOfStockEvents.forEachIndexed { index, event ->
                if (index == 0) {
                    if (event.stockOrder!!.orderAction != "Buy") {
                        throw IllegalStateException("First order must be a buy order!")
                    } else {
                        correctDecision += 1
                    }
                } else if (event.stockSplit != null) {
                    // Skip
                } else {
//                    val splits =
//                        getTotalSplit(listOfStockEvents.subList(0, index).filter { event.stockSplit != null }
//                            .map { event.stockSplit!! })
                    val currencyRateInSek = (currencyRateRepository.currencyRates.value as
                            CurrencyRateRepository.ViewState.VALUES).currencyRates.find { currencyRate -> currencyRate.iso4217CurrencySymbol == event.stockOrder!!.currency }!!.rateInSek
                    val recommendation = recommendationAlgorithm.getRecommendation(
                        StockPrice(
                            event.stockOrder!!.name,
                            Currency.getInstance(event.stockOrder!!.currency),
                            event.stockOrder!!.pricePerStock
                        ),
                        currencyRateInSek,
                        event.stockOrder!!.commissionFee,
                        listOfStockEvents.subList(0, index).toSet(),
                        event.dateInMillis
                    )
                    if (event.stockOrder!!.orderAction == "Buy") {
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

    private fun getTotalSplit(splits: List<StockSplit>): Int {
        var result = 1
        for (split in splits) {
            result *= if (split.reverse) {
                -1 * split.quantity
            } else {
                split.quantity
            }
        }
        return result
    }

}
