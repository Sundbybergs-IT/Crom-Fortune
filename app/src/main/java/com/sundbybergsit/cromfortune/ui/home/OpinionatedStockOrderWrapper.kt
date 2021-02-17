package com.sundbybergsit.cromfortune.ui.home

import com.sundbybergsit.cromfortune.stocks.StockOrder

class OpinionatedStockOrderWrapper(val stockOrder: StockOrder, val recommendation: Recommendation?) {

    fun isApprovedByAlgorithm(): Boolean {
        return recommendation != null && ((recommendation.command is BuyStockCommand) == (stockOrder.orderAction == "Buy"))
    }

}
