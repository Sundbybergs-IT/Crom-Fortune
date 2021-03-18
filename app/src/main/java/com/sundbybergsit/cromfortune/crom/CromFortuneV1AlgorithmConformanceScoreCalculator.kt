package com.sundbybergsit.cromfortune.crom

import android.util.Log
import com.sundbybergsit.cromfortune.algorithm.BuyStockCommand
import com.sundbybergsit.cromfortune.algorithm.ConformanceScore
import com.sundbybergsit.cromfortune.algorithm.RecommendationAlgorithm
import com.sundbybergsit.cromfortune.algorithm.SellStockCommand
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.stocks.StockOrder
import com.sundbybergsit.cromfortune.stocks.StockPrice
import java.util.*

class CromFortuneV1AlgorithmConformanceScoreCalculator : AlgorithmConformanceScoreCalculator() {

    companion object {

        private const val TAG = "CromFortuneV1AlgorithmC"

    }

    override suspend fun getScore(
            recommendationAlgorithm: RecommendationAlgorithm,
            orders: Set<StockOrder>,
            currencyRateRepository: CurrencyRateRepository,
    ): ConformanceScore {
        var correctDecision = 0
        val sortedOrders: MutableList<StockOrder> = orders.toMutableList()
        val stockNames = sortedOrders.map { order -> order.name }.toSet()

        // FIXME: recommend in groups of stocks

        val listOfListOfStockOrders: MutableList<List<StockOrder>> = mutableListOf()
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
                    val recommendation = recommendationAlgorithm.getRecommendation(StockPrice(order.name,
                            Currency.getInstance(order.currency),
                            order.pricePerStock), currencyRateInSek, order.commissionFee, listOfStockOrders.subList(0, index).toSet())
                    if (order.orderAction == "Buy") {
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
            orders.size <= 1 -> {
                ConformanceScore(100)
            }
            correctDecision == 0 -> {
                ConformanceScore(0)
            }
            else -> {
                ConformanceScore(100 * correctDecision / orders.size)
            }
        }
    }

}
