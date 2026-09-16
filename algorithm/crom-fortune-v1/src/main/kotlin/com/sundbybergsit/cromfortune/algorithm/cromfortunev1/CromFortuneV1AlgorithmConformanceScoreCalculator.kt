package com.sundbybergsit.cromfortune.algorithm.cromfortunev1

import android.util.Log
import com.sundbybergsit.cromfortune.algorithm.api.RecommendationAlgorithm
import com.sundbybergsit.cromfortune.algorithm.core.AlgorithmConformanceScoreCalculator
import com.sundbybergsit.cromfortune.algorithm.core.BuyStockCommand
import com.sundbybergsit.cromfortune.algorithm.core.ConformanceScore
import com.sundbybergsit.cromfortune.algorithm.core.SellStockCommand
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderAggregate
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRateApi
import java.util.Currency

class CromFortuneV1AlgorithmConformanceScoreCalculator : AlgorithmConformanceScoreCalculator() {

    companion object {

        private const val TAG = "CromFortuneV1AlgorithmC"

    }

    override suspend fun getScore(
        recommendationAlgorithm: RecommendationAlgorithm,
        stockEvents: Set<StockEvent>,
        currencyRateApi: CurrencyRateApi,
    ): ConformanceScore {
        var correctDecision = 0
        val stockOrders: List<StockOrder> = stockEvents
            .filter { stockEvent -> stockEvent.stockOrder != null }
            .map { stockEvent -> stockEvent.stockOrder!! }
        val currencyRatesByCode = currencyRateApi.currencyRates.value.associateBy {
            currencyRate -> currencyRate.iso4217CurrencySymbol
        }
        // Rates are populated asynchronously. An asset cannot be scored reliably until
        // its rate exists, so omit it for this refresh instead of crashing the UI.
        val evaluableStockOrders = stockOrders.filter { order ->
            currencyRatesByCode.containsKey(order.currency)
        }
        val stockNames = evaluableStockOrders.map { order -> order.name }.toSet()

        // FIXME: recommend in groups of stocks

        for (stockName in stockNames) {
            var firstItemAdded = false
            val stockOrdersForAsset: MutableList<StockOrder> = stockEvents
                .filter { stockEvent -> stockEvent.stockOrder != null && stockEvent.stockOrder!!.name == stockName }
                .map { stockEvent -> stockEvent.stockOrder!! }
                .sortedBy { stockOrder -> stockOrder.dateInMillis }.toMutableList()
            val firstStockOrderForStock = stockOrdersForAsset.first()
            val currencyRateInSek = currencyRatesByCode.getValue(firstStockOrderForStock.currency).rateInSek
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
            evaluableStockOrders.size <= 1 -> {
                ConformanceScore(100)
            }

            correctDecision == 0 -> {
                ConformanceScore(0)
            }

            else -> {
                ConformanceScore(100 * correctDecision / evaluableStockOrders.size)
            }
        }
    }

}
