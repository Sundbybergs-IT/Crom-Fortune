package com.sundbybergsit.cromfortune.domain

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import java.math.BigDecimal

// Name == Stock symbol
@Serializable
@OptIn(ExperimentalSerializationApi::class)
data class StockOrder(
    val orderAction: String,
    val currency: String,
    val dateInMillis: Long,
    val name: String,
    val pricePerStock: Double,
    val commissionFee: Double = 0.0,
    @Serializable(with = BigDecimalAsStringSerializer::class)
    val quantity: BigDecimal,
    @EncodeDefault val assetId: String = "stock:$name",
    @EncodeDefault val assetType: AssetType = AssetType.STOCK,
    @EncodeDefault val schemaVersion: Int = CURRENT_SCHEMA_VERSION
) {

    constructor(
        orderAction: String,
        currency: String,
        dateInMillis: Long,
        name: String,
        pricePerStock: Double,
        commissionFee: Double = 0.0,
        quantity: Int
    ) : this(
        orderAction = orderAction,
        currency = currency,
        dateInMillis = dateInMillis,
        name = name,
        pricePerStock = pricePerStock,
        commissionFee = commissionFee,
        quantity = quantity.toBigDecimal()
    )

    fun getAcquisitionValue(rateInSek: Double): Double {
        return if (orderAction == "Buy") {
            (quantity.toDouble() * pricePerStock + (commissionFee / rateInSek)) / quantity.toDouble()
        } else {
            0.0
        }
    }

    fun getTotalCost(rateInSek: Double): Double {
        return if (orderAction == "Buy") {
            quantity.toDouble() * pricePerStock + (commissionFee / rateInSek)
        } else {
            -quantity.toDouble() * pricePerStock + (commissionFee / rateInSek)
        }
    }

    fun toStockEvent(): StockEvent {
        return StockEvent(this, null, dateInMillis)
    }

    companion object {
        const val CURRENT_SCHEMA_VERSION = 2
    }

}
