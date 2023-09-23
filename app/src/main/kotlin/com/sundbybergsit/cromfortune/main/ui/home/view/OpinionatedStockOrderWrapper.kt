package com.sundbybergsit.cromfortune.main.ui.home.view

import com.sundbybergsit.cromfortune.algorithm.api.Recommendation
import com.sundbybergsit.cromfortune.algorithm.core.BuyStockCommand

internal class OpinionatedStockOrderWrapper(val stockOrder: com.sundbybergsit.cromfortune.domain.StockOrder, val recommendation: Recommendation?) {

    fun isApprovedByAlgorithm(): Boolean {
        return recommendation != null &&
                ((recommendation.command is BuyStockCommand) == (stockOrder.orderAction == "Buy"))
    }

}
