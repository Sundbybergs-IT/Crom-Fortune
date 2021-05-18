package com.sundbybergsit.cromfortune.crom

import android.util.Log
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import java.util.*

class CromFortuneV1AlgorithmConformanceScoreCalculator : AlgorithmConformanceScoreCalculator() {

    companion object {

        private const val TAG = "CromFortuneV1AlgorithmC"

    }

    override suspend fun getScore(
        recommendationAlgorithm: com.sundbybergsit.cromfortune.algorithm.RecommendationAlgorithm,
        orders: Set<com.sundbybergsit.cromfortune.domain.StockOrder>,
        currencyRateRepository: CurrencyRateRepository,
    ): com.sundbybergsit.cromfortune.algorithm.ConformanceScore {
        var correctDecision = 0
        val sortedOrders: MutableList<com.sundbybergsit.cromfortune.domain.StockOrder> = orders.toMutableList()
        val stockNames = sortedOrders.map { order -> order.name }.toSet()

        // FIXME: recommend in groups of stocks

        val listOfListOfStockOrders: MutableList<List<com.sundbybergsit.cromfortune.domain.StockOrder>> = mutableListOf()
        for (stockName in stockNames) {
            listOfListOfStockOrders.add(sortedOrders.filter { stockOrder -> stockOrder.name == stockName }.sortedBy { order -> order.dateInMillis })
        }
        for (listOfStockOrders in listOfListOfStockOrders) {
            listOfStockOrders.forEachIndexed { index, order ->
                if (index == 0) {
                    if (order.orderAction != "Buy") {
                        throw IllegalStateException("First order must be a buy order!")
                    } else {
                        correctDecision += 1
                    }
                } else {
                    val currencyRateInSek = (currencyRateRepository.currencyRates.value as
                            CurrencyRateRepository.ViewState.VALUES).currencyRates.find { currencyRate -> currencyRate.iso4217CurrencySymbol == order.currency }!!.rateInSek
                    val recommendation = recommendationAlgorithm.getRecommendation(
                        com.sundbybergsit.cromfortune.domain.StockPrice(
                            order.name,
                            Currency.getInstance(order.currency),
                            order.pricePerStock
                        ), currencyRateInSek, order.commissionFee, listOfStockOrders.subList(0, index).toSet(),
                            order.dateInMillis)
                    if (order.orderAction == "Buy") {
                        if (recommendation != null && recommendation.command is com.sundbybergsit.cromfortune.algorithm.BuyStockCommand) {
                            correctDecision += 1
                        } else {
                            Log.v(TAG, "Bad decision.")
                        }
                    } else {
                        if (recommendation != null && recommendation.command is com.sundbybergsit.cromfortune.algorithm.SellStockCommand) {
                            correctDecision += 1
                        } else {
                            Log.v(TAG, "Bad decision.")
                        }
                    }
                }
            }
        }
        return when {
            orders.size <= 1 -> {
                com.sundbybergsit.cromfortune.algorithm.ConformanceScore(100)
            }
            correctDecision == 0 -> {
                com.sundbybergsit.cromfortune.algorithm.ConformanceScore(0)
            }
            else -> {
                com.sundbybergsit.cromfortune.algorithm.ConformanceScore(100 * correctDecision / orders.size)
            }
        }
    }

}
