package com.sundbybergsit.cromfortune.ui.home

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class CromFortuneV1RecommendationAlgorithm(private val context: Context)
    : RecommendationAlgorithm() {

    companion object {

        const val DIFF_PERCENTAGE: Double = .10

    }

    override suspend fun getRecommendation(
            stockPrice: StockPrice, commissionFee: Double,
            currencyConversionRateProducer: CurrencyConversionRateProducer,
            previousOrders: Set<StockOrder>,
    ): Recommendation? {
        return withContext(Dispatchers.IO) {
            RecommendationGenerator(context, currencyConversionRateProducer).getRecommendation(stockPrice.name,
                    previousOrders, stockPrice.price,
                    commissionFee)
        }

    }

    internal class RecommendationGenerator(
            private val context: Context,
            private val currencyConversationRateProducer: CurrencyConversionRateProducer,
    ) {

        fun getRecommendation(
                stockName: String, orders: Set<StockOrder>, currentStockPriceInStockCurrency: Double,
                commissionFeeInSek: Double,
        ): Recommendation? {
            var grossQuantity = 0
            var soldQuantity = 0
            var accumulatedCostInSek = 0.0
            var rate = 1.0
            var currency: Currency? = null
            for (stockOrder in orders) {
                if (stockOrder.name == stockName) {
                    if (currency == null) {
                        currency = Currency.getInstance(stockOrder.currency)
                        rate = currencyConversationRateProducer.getRateInSek(currency)
                    } else {
                        if (currency != Currency.getInstance(stockOrder.currency)) {
                            throw IllegalStateException("Cannot mix currencies for a stock!")
                        }
                    }
                    if (stockOrder.orderAction == "Buy") {
                        grossQuantity += stockOrder.quantity
                        accumulatedCostInSek += rate * stockOrder.pricePerStock * stockOrder.quantity +
                                stockOrder.commissionFee
                    } else {
                        soldQuantity += stockOrder.quantity
                    }
                }
            }
            val netQuantity = grossQuantity - soldQuantity
            val averageCostInSek = accumulatedCostInSek / grossQuantity
            val costToExcludeInSek = averageCostInSek * soldQuantity

            val totalPricePerStockInSek = (accumulatedCostInSek - costToExcludeInSek) / netQuantity
            val totalPricePerStockInStockCurrency = totalPricePerStockInSek / rate
            val currentTimeInMillis = System.currentTimeMillis()
            val potentialBuyQuantity = netQuantity / 10
            val pricePerStockAfterBuyInStockCurrency = ((netQuantity * totalPricePerStockInStockCurrency + potentialBuyQuantity * currentStockPriceInStockCurrency + commissionFeeInSek) / (netQuantity + potentialBuyQuantity))
            if (currentStockPriceInStockCurrency < (1 - DIFF_PERCENTAGE) * pricePerStockAfterBuyInStockCurrency) {
                if (potentialBuyQuantity > 0) {
                    return Recommendation(BuyStockCommand(context, currentTimeInMillis, currency!!, stockName,
                            currentStockPriceInStockCurrency, potentialBuyQuantity, commissionFeeInSek))
                }
            } else if (currentStockPriceInStockCurrency > ((1 + DIFF_PERCENTAGE) * totalPricePerStockInStockCurrency) + commissionFeeInSek / rate) {
                val quantity = netQuantity / 10
                if (quantity > 0) {
                    return Recommendation(SellStockCommand(context, currentTimeInMillis, currency!!, stockName,
                            currentStockPriceInStockCurrency, quantity, commissionFeeInSek))
                }
            }
            return null
        }

    }

}
