package com.sundbybergsit.cromfortune.ui.home

import android.content.Context
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class CromFortuneV1RecommendationAlgorithm(private val context: Context) : RecommendationAlgorithm() {

    companion object {

        const val DIFF_PERCENTAGE: Double = .10

    }

    override suspend fun getRecommendation(
            stockPrice: StockPrice, commissionFee: Double, previousOrders: Set<StockOrder>,
    ): Recommendation? {
        return withContext(Dispatchers.IO) {
            RecommendationGenerator(context).getRecommendation(stockPrice.name, previousOrders, stockPrice.price,
                    commissionFee)
        }

    }

    internal class RecommendationGenerator(private val context: Context) {

        fun getRecommendation(
                stockName: String, orders: Set<StockOrder>, currentStockPriceInStockCurrency: Double, commissionFeeInSek: Double,
        ): Recommendation? {
            if (orders.isEmpty()) {
                return null
            }
            var grossQuantity = 0
            var soldQuantity = 0
            var accumulatedCostInSek = 0.0
            var currency: Currency? = null
            var rateInSek: Double? = null
            for (stockOrder in orders) {
                if (stockOrder.name == stockName) {
                    if (currency == null) {
                        currency = Currency.getInstance(stockOrder.currency)
                    } else {
                        if (currency != Currency.getInstance(stockOrder.currency)) {
                            throw IllegalStateException("Cannot mix currencies for a stock!")
                        }
                    }
                    if (rateInSek == null) {
                        val latestCurrencyRatesViewState = CurrencyRateRepository.currencyRates.value
                                as CurrencyRateRepository.ViewState.VALUES
                        rateInSek =
                                latestCurrencyRatesViewState.currencyRates.find { currencyRate -> currencyRate.iso4217CurrencySymbol == stockOrder.currency }!!.rateInSek
                    }
                    if (stockOrder.orderAction == "Buy") {
                        grossQuantity += stockOrder.quantity
                        accumulatedCostInSek += rateInSek * stockOrder.pricePerStock * stockOrder.quantity +
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
            val totalPricePerStockInStockCurrency = totalPricePerStockInSek / rateInSek!!
            val currentTimeInMillis = System.currentTimeMillis()
            val potentialBuyQuantity = netQuantity / 10
            val pricePerStockAfterBuyInStockCurrency = ((netQuantity * averageCostInSek +
                    potentialBuyQuantity * currentStockPriceInStockCurrency * rateInSek + commissionFeeInSek) /
                    (netQuantity + potentialBuyQuantity)) / rateInSek
            if (currentStockPriceInStockCurrency < (1 - DIFF_PERCENTAGE) * pricePerStockAfterBuyInStockCurrency) {
                if (potentialBuyQuantity > 0) {
                    return Recommendation(BuyStockCommand(context, currentTimeInMillis, currency!!, stockName,
                            currentStockPriceInStockCurrency, potentialBuyQuantity, commissionFeeInSek))
                }
            } else if (currentStockPriceInStockCurrency > ((1 + DIFF_PERCENTAGE) * totalPricePerStockInStockCurrency) + commissionFeeInSek / rateInSek) {
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
