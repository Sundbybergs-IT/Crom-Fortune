package com.sundbybergsit.cromfortune.ui.home

class CromFortuneV1AlgorithmConformanceScoreCalculator : AlgorithmConformanceScoreCalculator() {

    override suspend fun getScore(recommendationAlgorithm: RecommendationAlgorithm, orders: Set<StockOrder>): ConformanceScore {
        var correctDecision = 0
        val sortedOrders: MutableList<StockOrder> = orders.toMutableList()
        sortedOrders.sortBy { order -> order.dateInMillis }
        sortedOrders.forEachIndexed { index, order ->
            if (index == 0) {
                if (order.orderAction != "Buy") {
                    throw IllegalStateException("First order must be a buy order!")
                }
            } else {
                val recommendation = recommendationAlgorithm.getRecommendation(StockPrice(order.name, order.pricePerStock),
                        order.commissionFee, CurrencyConversionRateProducer(),
                        sortedOrders.subList(0, index).toSet())
                if (order.orderAction == "Buy") {
                    if (recommendation != null && recommendation.command is BuyStockCommand) {
                        correctDecision += 1
                    }
                } else {
                    if (recommendation != null && recommendation.command is SellStockCommand) {
                        correctDecision += 1
                    }
                }
            }
        }
        return when {
            orders.size <= 1 -> {
                ConformanceScore(100)
            }
            correctDecision == 0 -> {
                ConformanceScore(0)
            }
            else -> {
                ConformanceScore(100 * correctDecision / (orders.size - 1))
            }
        }
    }

}