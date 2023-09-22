package com.sundbybergsit.cromfortune.domain

data class StockEvent(val stockOrder: StockOrder?, val stockSplit: StockSplit?, val dateInMillis: Long) {

    init {
        require(stockOrder != null || stockSplit != null) {
            "At least one parameter must be set"
        }
    }

}
