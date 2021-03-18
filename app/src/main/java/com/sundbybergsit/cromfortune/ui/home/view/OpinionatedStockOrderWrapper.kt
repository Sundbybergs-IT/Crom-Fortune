package com.sundbybergsit.cromfortune.ui.home.view

import com.sundbybergsit.cromfortune.algorithm.BuyStockCommand
import com.sundbybergsit.cromfortune.algorithm.Recommendation
import com.sundbybergsit.cromfortune.stocks.StockOrder

internal class OpinionatedStockOrderWrapper(val stockOrder: StockOrder, val recommendation: Recommendation?) {

    fun isApprovedByAlgorithm(): Boolean {
        return recommendation != null &&
                ((recommendation.command is BuyStockCommand) == (stockOrder.orderAction == "Buy"))
    }

}
