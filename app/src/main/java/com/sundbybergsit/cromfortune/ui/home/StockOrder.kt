package com.sundbybergsit.cromfortune.ui.home

import kotlinx.serialization.Serializable
import java.util.*

// Name == Stock symbol
@Serializable
data class StockOrder(
        val orderAction: String, val currency: String, val dateInMillis: Long, val name: String,
        val pricePerStock: Double, val commissionFee: Double = 0.0, val quantity: Int,
) {

    fun getAcquisitionValue(currencyConversionRateProducer: CurrencyConversionRateProducer): Double {
        return if (orderAction == "Buy") {
            (quantity * pricePerStock + (commissionFee / currencyConversionRateProducer.getRateInSek(Currency.getInstance(currency)))) / quantity
        } else {
            0.0
        }
    }

    fun getTotalCost(currencyConversionRateProducer: CurrencyConversionRateProducer): Double {
        return if (orderAction == "Buy") {
            quantity * pricePerStock + (commissionFee / currencyConversionRateProducer.getRateInSek(Currency.getInstance(currency)))
        } else {
            -quantity * pricePerStock + (commissionFee / currencyConversionRateProducer.getRateInSek(Currency.getInstance(currency)))
        }
    }

}
