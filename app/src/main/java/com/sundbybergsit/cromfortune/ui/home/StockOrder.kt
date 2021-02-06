package com.sundbybergsit.cromfortune.ui.home

import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import kotlinx.serialization.Serializable

// Name == Stock symbol
@Serializable
data class StockOrder(
        val orderAction: String, val currency: String, val dateInMillis: Long, val name: String,
        val pricePerStock: Double, val commissionFee: Double = 0.0, val quantity: Int,
) {

    fun getAcquisitionValue(): Double {
        val rateInSek = getRateInSek()
        return if (orderAction == "Buy") {
            (quantity * pricePerStock + (commissionFee / rateInSek)) / quantity
        } else {
            0.0
        }
    }

    fun getTotalCost(): Double {
        val rateInSek = getRateInSek()
        return if (orderAction == "Buy") {
            quantity * pricePerStock + (commissionFee / rateInSek)
        } else {
            -quantity * pricePerStock + (commissionFee / rateInSek)
        }
    }

    private fun getRateInSek(): Double = (CurrencyRateRepository.currencyRates.value as CurrencyRateRepository.ViewState.VALUES)
            .currencyRates.find { currencyRate -> currencyRate.iso4217CurrencySymbol == currency }!!.rateInSek

}
