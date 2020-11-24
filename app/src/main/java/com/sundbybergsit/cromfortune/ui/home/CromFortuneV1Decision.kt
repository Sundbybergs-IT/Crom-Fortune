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

    override fun getRecommendation(stockPrice: StockPrice, commissionFee: Double): Recommendation? {
        val recommendation = RecommendationGenerator(context).getRecommendation(stockPrice.name,
                sharedPreferences.getStringSet(stockPrice.name, emptySet()) as Set<String>, stockPrice.price,
                commissionFee)
        if (recommendation != null) {
            return recommendation
        }
        return null
    }

    internal class RecommendationGenerator(private val context: Context) {

        fun getRecommendation(stockName: String, orders: Set<String>, currentStockPrice: Double,
                              commissionFee: Double): Recommendation? {
            var accumulatedCommissionFees = 0.0
            var accumulatedQuantity = 0
            var accumulatedCost = 0.0
            var currency : Currency? = null
            for (serializedOrder in orders) {
                val stockOrder: StockOrder = Json.decodeFromString(serializedOrder)
                if (stockOrder.name == stockName) {
                    if (currency == null) {
                        currency = Currency.getInstance(stockOrder.currency)
                    } else {
                        if (currency != Currency.getInstance(stockOrder.currency)) {
                            throw IllegalStateException("Cannot mix currencies for a stock!")
                        }
                    }
                    accumulatedCommissionFees += stockOrder.commissionFee
                    accumulatedQuantity += stockOrder.quantity
                    accumulatedCost += stockOrder.pricePerStock * stockOrder.quantity
                }
            }
            val totalPricePerStock = (accumulatedCost + accumulatedCommissionFees) / accumulatedQuantity
            val currentTimeInMillis = System.currentTimeMillis()
            val potentialBuyQuantity = accumulatedQuantity / 10
            val pricePerStockAfterBuy = (accumulatedCost + accumulatedCommissionFees + commissionFee) /
                    (accumulatedQuantity + potentialBuyQuantity)
            if (currentStockPrice < (1 - DIFF_PERCENTAGE) * pricePerStockAfterBuy) {
                if (potentialBuyQuantity > 0) {
                    return Recommendation(BuyStockCommand(context, currentTimeInMillis, currency!!, stockName,
                            currentStockPrice, potentialBuyQuantity, commissionFee))
                }
            } else if (currentStockPrice > (1 + DIFF_PERCENTAGE) * totalPricePerStock) {
                val quantity = accumulatedQuantity / 10
                if (quantity > 0) {
                    return Recommendation(SellStockCommand(context, currentTimeInMillis, currency!!, stockName,
                            currentStockPrice, quantity, commissionFee))
                }
            }
            return null
        }

    }

}

