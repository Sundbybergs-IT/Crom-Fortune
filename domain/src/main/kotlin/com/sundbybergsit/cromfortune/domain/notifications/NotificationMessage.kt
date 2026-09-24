package com.sundbybergsit.cromfortune.domain.notifications

import kotlinx.serialization.Serializable

@Serializable
data class NotificationMessage(
    val dateInMillis: Long,
    val message: String,
    val portfolioName: String? = null,
    val stockSymbol: String? = null,
    val currencyCode: String? = null,
    val pricePerStock: Double? = null
)
