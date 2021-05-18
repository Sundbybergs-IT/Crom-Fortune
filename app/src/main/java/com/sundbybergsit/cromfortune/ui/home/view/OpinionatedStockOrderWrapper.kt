package com.sundbybergsit.cromfortune.ui.home.view

internal class OpinionatedStockOrderWrapper(val stockOrder: com.sundbybergsit.cromfortune.domain.StockOrder, val recommendation: com.sundbybergsit.cromfortune.algorithm.Recommendation?) {

    fun isApprovedByAlgorithm(): Boolean {
        return recommendation != null &&
                ((recommendation.command is com.sundbybergsit.cromfortune.algorithm.BuyStockCommand) == (stockOrder.orderAction == "Buy"))
    }

}
