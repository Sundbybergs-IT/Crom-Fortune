package com.sundbybergsit.cromfortune.ui.home

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*

class CromFortuneV1Decision(private val context: Context,
                            private val sharedPreferences: SharedPreferences =
                                    context.getSharedPreferences("Stocks", Context.MODE_PRIVATE)) : Decision() {

    companion object {

        const val DIFF_PERCENTAGE: Double = .10

    }

    override fun getRecommendation(currency: Currency, stockPrice: StockPrice, commissionFee: Double): Recommendation? {
        val recommendation = RecommendationGenerator(context).getRecommendation(stockPrice.name,
                currency, sharedPreferences.getStringSet(stockPrice.name, emptySet()) as Set<String>, stockPrice.price,
                commissionFee)
        if (recommendation != null) {
            return recommendation
        }
        return null
    }

    internal class RecommendationGenerator(private val context: Context) {

        fun getRecommendation(stockName: String, currency: Currency, orders: Set<String>, currentStockPrice: Double,
                              commissionFee: Double): Recommendation? {
            var accumulatedCommissionFees = 0.0
            var accumulatedQuantity = 0
            var accumulatedCost = 0.0
            for (serializedOrder in orders) {
                val stockOrder: StockOrder = Json.decodeFromString(serializedOrder)
                if (stockOrder.name == stockName) {
                    accumulatedCommissionFees += stockOrder.commissionFee
                    accumulatedQuantity += stockOrder.quantity
                    accumulatedCost += stockOrder.pricePerStock * stockOrder.quantity
                }
            }
            val totalPricePerStock = (accumulatedCost + accumulatedCommissionFees) / accumulatedQuantity
            val currentTimeInMillis = System.currentTimeMillis()
            val potentialBuyQuantity = accumulatedQuantity / 10
            val pricePerStockAfterBuy = (accumulatedCost + accumulatedCommissionFees + commissionFee) / (accumulatedQuantity + potentialBuyQuantity)
            if (currentStockPrice < (1 - DIFF_PERCENTAGE) * pricePerStockAfterBuy) {
                if (potentialBuyQuantity > 0) {
                    return Recommendation(BuyStockCommand(context, currentTimeInMillis, currency, stockName,
                            currentStockPrice, potentialBuyQuantity, commissionFee))
                }
            } else if (currentStockPrice > (1 + DIFF_PERCENTAGE) * totalPricePerStock) {
                val quantity = accumulatedQuantity / 10
                if (quantity > 0) {
                    return Recommendation(SellStockCommand(context, currentTimeInMillis, currency, stockName, currentStockPrice, quantity))
                }
            }
            return null
        }

    }

}

