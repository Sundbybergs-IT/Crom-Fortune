package com.sundbybergsit.cromfortune.ui.home

import android.content.Context
import com.sundbybergsit.cromfortune.stocks.StockOrderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class CromFortuneV1RecommendationAlgorithm(private val context: Context,
                                           private val stockOrderRepository: StockOrderRepository = StockOrderRepositoryImpl(context))
    : RecommendationAlgorithm() {

    companion object {

        const val DIFF_PERCENTAGE: Double = .10

    }

    override suspend fun getRecommendation(stockPrice: StockPrice, commissionFee: Double,
                                           currencyConversionRateProducer: CurrencyConversionRateProducer,
                                           previousOrders: Set<StockOrder>): Recommendation? {
        return withContext(Dispatchers.IO) {
            RecommendationGenerator(context, currencyConversionRateProducer).getRecommendation(stockPrice.name,
                    previousOrders, stockPrice.price,
                    commissionFee)
        }

    }

    internal class RecommendationGenerator(private val context: Context,
                                           private val currencyConversationRateProducer: CurrencyConversionRateProducer) {

        fun getRecommendation(stockName: String, orders: Set<StockOrder>, currentStockPriceInStockCurrency: Double,
                              commissionFeeInSek: Double): Recommendation? {
            var accumulatedCommissionFeesInSek = 0.0
            var accumulatedQuantity = 0
            var accumulatedCostInSek = 0.0
            var currency: Currency? = null
            var rate = 1.0
            for (stockOrder in orders) {
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
