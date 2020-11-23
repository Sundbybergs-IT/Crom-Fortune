package com.sundbybergsit.cromfortune.ui.home

import kotlinx.serialization.Serializable

@Serializable
data class StockOrder(val orderAction: String, val currency: String, val dateInMillis: Long, val name: String,
                      val pricePerStock: Double, val commissionFee: Double = 0.0, val quantity: Int) {

    fun getAcquisitionValue(): Double {
        return (quantity * pricePerStock + commissionFee) / quantity
    }

}
