package com.sundbybergsit.cromfortune.ui.home

abstract class Decision {

    abstract suspend fun getRecommendation(stockPrice: StockPrice, commissionFee: Double,
                                           currencyConversionRateProducer: CurrencyConversionRateProducer): Recommendation?

}
