package com.sundbybergsit.cromfortune.ui.home

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class CromFortuneV1Decision(private val context: Context,
                            private val sharedPreferences: SharedPreferences =
                                    context.getSharedPreferences("Stocks", Context.MODE_PRIVATE)) : Decision() {

    companion object {

        const val DIFF_PERCENTAGE: Double = .10

    }

    override fun getRecommendation(stockPrice: StockPrice, commissionFee : Double): Recommendation? {
            val recommendation = RecommendationGenerator(context).getRecommendation(stockPrice.name,
                    sharedPreferences.getStringSet(stockPrice.name, emptySet()) as Set<String>, stockPrice.price,
            commissionFee)
            if (recommendation != null) {
                return recommendation
            }
        return null
    }

    internal class RecommendationGenerator(private val context: Context) {

        fun getRecommendation(stockName: String, orders: Set<String>, currentStockPrice: Double, commissionFee: Double): Recommendation? {
            for (serializedOrder in orders) {
                val stockOrder: StockOrder = Json.decodeFromString(serializedOrder)
                if (currentStockPrice < (stockOrder.commissionFee + (1 - DIFF_PERCENTAGE) * stockOrder.pricePerStock)) {
                    val quantity = stockOrder.quantity / 10
                    if (quantity > 0) {
                        return Recommendation(BuyStockCommand(context, System.currentTimeMillis(), stockName,
                                currentStockPrice, quantity, commissionFee))
                    }
                } else if (currentStockPrice > (1 + DIFF_PERCENTAGE) * stockOrder.pricePerStock) {
                    val quantity = stockOrder.quantity / 10
                    if (quantity > 0) {
                        return Recommendation(SellStockCommand(context, stockName, currentStockPrice, quantity))
                    }
                }
            }
            return null
        }
    }

}
