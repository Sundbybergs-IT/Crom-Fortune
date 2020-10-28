package com.sundbybergsit.cromfortune.ui.home

abstract class Decision {

    abstract fun getRecommendation(stockPrice: StockPrice, commissionFee : Double): Recommendation?

}
