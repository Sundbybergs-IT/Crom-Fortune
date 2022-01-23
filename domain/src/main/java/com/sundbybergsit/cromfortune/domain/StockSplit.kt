package com.sundbybergsit.cromfortune.domain

import kotlinx.serialization.Serializable

// Name == Stock symbol
@Serializable
data class StockSplit(val reverse: Boolean, val dateInMillis: Long, val name: String, val quantity: Int) {

    fun toStockEvent(): StockEvent {
        return StockEvent(null, this, dateInMillis)
    }

}
