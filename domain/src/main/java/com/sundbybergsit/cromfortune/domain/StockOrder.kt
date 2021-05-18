package com.sundbybergsit.cromfortune.domain

import kotlinx.serialization.Serializable

// Name == Stock symbol
@Serializable
data class StockOrder(
        val orderAction: String, val currency: String, val dateInMillis: Long, val name: String,
        val pricePerStock: Double, val commissionFee: Double = 0.0, val quantity: Int,
) {

    fun getAcquisitionValue(rateInSek: Double): Double {
        return if (orderAction == "Buy") {
            (quantity * pricePerStock + (commissionFee / rateInSek)) / quantity
        } else {
            0.0
        }
    }

    fun getTotalCost(rateInSek: Double): Double {
        return if (orderAction == "Buy") {
            quantity * pricePerStock + (commissionFee / rateInSek)
        } else {
            -quantity * pricePerStock + (commissionFee / rateInSek)
        }
    }

}
