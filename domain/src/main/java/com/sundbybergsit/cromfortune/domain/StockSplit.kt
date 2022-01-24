package com.sundbybergsit.cromfortune.domain

import androidx.annotation.IntRange
import kotlinx.serialization.Serializable

// Name == Stock symbol
@Serializable
data class StockSplit(
    val reverse: Boolean,
    val dateInMillis: Long,
    val name: String,
    @IntRange(from = MIN_QUANTITY.toLong(), to = Int.MAX_VALUE.toLong()) val quantity: Int
) {

    companion object {

        const val MIN_QUANTITY = 2

    }

    fun toStockEvent(): StockEvent {
        return StockEvent(null, this, dateInMillis)
    }

}
