package com.sundbybergsit.cromfortune.main.ui.home.view

import com.sundbybergsit.cromfortune.algorithm.BuyStockCommand
import com.sundbybergsit.cromfortune.algorithm.Recommendation

internal class OpinionatedStockOrderWrapper(val stockOrder: com.sundbybergsit.cromfortune.domain.StockOrder, val recommendation: Recommendation?) {

    fun isApprovedByAlgorithm(): Boolean {
        return recommendation != null &&
                ((recommendation.command is BuyStockCommand) == (stockOrder.orderAction == "Buy"))
    }

}
