package com.sundbybergsit.cromfortune.algorithm.api

import com.sundbybergsit.cromfortune.domain.AssetTransactionApi
import java.math.BigDecimal

interface AssetTransactionCommand : Command<AssetTransactionApi> {
    val assetId: String
    val quantity: BigDecimal
    val unitPrice: BigDecimal
    val commissionFee: BigDecimal
}
