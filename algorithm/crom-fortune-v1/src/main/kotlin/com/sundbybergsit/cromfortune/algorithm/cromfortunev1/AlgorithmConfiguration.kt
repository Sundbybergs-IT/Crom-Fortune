package com.sundbybergsit.cromfortune.algorithm.cromfortunev1

data class AlgorithmConfiguration(
    val firstPurchaseOrderInSek: Double,
    val maxPurchaseOrderInSek: Double,
    val normalDiffPercentage: Double,
    val maxBuyPercentage: Double,
    val maxSoldPercentage: Double,
    val maxExtremeBuyPercentage: Double,
    val maxExtremeSoldPercentage: Double,
    val minFreezePeriodInDays: Long
)
