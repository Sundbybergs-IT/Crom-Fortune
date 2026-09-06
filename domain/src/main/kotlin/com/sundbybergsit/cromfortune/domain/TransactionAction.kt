package com.sundbybergsit.cromfortune.domain

import kotlinx.serialization.Serializable

@Serializable
enum class TransactionAction {
    BUY,
    SELL
}
