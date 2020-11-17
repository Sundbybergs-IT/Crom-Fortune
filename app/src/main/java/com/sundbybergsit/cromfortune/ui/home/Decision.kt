package com.sundbybergsit.cromfortune.ui.home

import java.util.*

abstract class Decision {

    abstract fun getRecommendation(currency: Currency, stockPrice: StockPrice, commissionFee: Double): Recommendation?

}
