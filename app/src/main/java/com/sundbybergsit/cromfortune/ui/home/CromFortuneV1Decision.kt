package com.sundbybergsit.cromfortune.ui.home

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*

class CromFortuneV1Decision(private val context: Context,
                            private val sharedPreferences: SharedPreferences =
                                    context.getSharedPreferences("Stocks", Context.MODE_PRIVATE)) : Decision() {

    companion object {

        const val DIFF_PERCENTAGE: Double = .10

    }

    override suspend fun getRecommendation(stockPrice: StockPrice, commissionFee: Double,
                                           currencyConversionRateProducer: CurrencyConversionRateProducer): Recommendation? {
        return withContext(Dispatchers.IO) {
            RecommendationGenerator(context, currencyConversionRateProducer).getRecommendation(stockPrice.name,
                    sharedPreferences.getStringSet(stockPrice.name, emptySet()) as Set<String>, stockPrice.price,
                    commissionFee)
        }

    }

    internal class RecommendationGenerator(private val context: Context,
                                           private val currencyConversationRateProducer: CurrencyConversionRateProducer) {

        fun getRecommendation(stockName: String, orders: Set<String>, currentStockPriceInStockCurrency: Double,
                              commissionFeeInSek: Double): Recommendation? {
            var accumulatedCommissionFeesInSek = 0.0
            var accumulatedQuantity = 0
            var accumulatedCostInSek = 0.0
            var currency: Currency? = null
            var rate = 1.0
            for (serializedOrder in orders) {
                val stockOrder: StockOrder = Json.decodeFromString(serializedOrder)
                if (stockOrder.name == stockName) {
                    if (currency == null) {
                        currency = Currency.getInstance(stockOrder.currency)
                        rate = currencyConversationRateProducer.getRateInSek(stockName)
                    } else {
                        if (currency != Currency.getInstance(stockOrder.currency)) {
                            throw IllegalStateException("Cannot mix currencies for a stock!")
                        }
                    }
                    accumulatedCommissionFeesInSek += stockOrder.commissionFee
                    accumulatedQuantity += stockOrder.quantity
                    accumulatedCostInSek += rate * stockOrder.pricePerStock * stockOrder.quantity
                }
            }
            val totalPricePerStockInSek = (accumulatedCostInSek + accumulatedCommissionFeesInSek) / accumulatedQuantity
            val totalPricePerStockInStockCurrency = totalPricePerStockInSek / rate
            val currentTimeInMillis = System.currentTimeMillis()
            val potentialBuyQuantity = accumulatedQuantity / 10
            val pricePerStockAfterBuyInStockCurrency = ((accumulatedCostInSek + accumulatedCommissionFeesInSek +
                    commissionFeeInSek) / (accumulatedQuantity + potentialBuyQuantity) / rate)
            if (currentStockPriceInStockCurrency < (1 - DIFF_PERCENTAGE) * pricePerStockAfterBuyInStockCurrency) {
                if (potentialBuyQuantity > 0) {
                    return Recommendation(BuyStockCommand(context, currentTimeInMillis, currency!!, stockName,
                            currentStockPriceInStockCurrency, potentialBuyQuantity, commissionFeeInSek))
                }
            } else if (currentStockPriceInStockCurrency > (1 + DIFF_PERCENTAGE) * totalPricePerStockInStockCurrency) {
                val quantity = accumulatedQuantity / 10
                if (quantity > 0) {
                    return Recommendation(SellStockCommand(context, currentTimeInMillis, currency!!, stockName,
                            currentStockPriceInStockCurrency, quantity, commissionFeeInSek))
                }
            }
            return null
        }

    }

}
