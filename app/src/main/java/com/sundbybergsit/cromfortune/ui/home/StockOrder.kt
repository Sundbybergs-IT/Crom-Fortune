package com.sundbybergsit.cromfortune.ui.home

import kotlinx.serialization.Serializable

@Serializable
data class StockOrder(val orderAction: String, val dateInMillis: Long, val name: String, val pricePerStock: Double,
                      val commissionFee: Double, val quantity: Int) {

    fun getAcquisitionValue(): Double {
        return (quantity * pricePerStock + commissionFee) / quantity
    }

}
