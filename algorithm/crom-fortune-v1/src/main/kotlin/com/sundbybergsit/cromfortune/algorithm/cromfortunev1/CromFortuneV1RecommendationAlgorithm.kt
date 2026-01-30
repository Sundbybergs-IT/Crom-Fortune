package com.sundbybergsit.cromfortune.algorithm.cromfortunev1

import com.sundbybergsit.cromfortune.algorithm.api.Recommendation
import com.sundbybergsit.cromfortune.algorithm.api.RecommendationAlgorithm
import com.sundbybergsit.cromfortune.algorithm.core.BuyStockCommand
import com.sundbybergsit.cromfortune.algorithm.core.SellStockCommand
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockPrice
import java.util.Currency
import java.util.concurrent.TimeUnit

class CromFortuneV1RecommendationAlgorithm(
    private val configuration: AlgorithmConfiguration = AlgorithmConfiguration(
        firstPurchaseOrderInSek = DEFAULT_FIRST_PURCHASE_ORDER_IN_SEK,
        maxPurchaseOrderInSek = MAX_PURCHASE_ORDER_IN_SEK,
        normalDiffPercentage = NORMAL_DIFF_PERCENTAGE,
        maxBuyPercentage = MAX_BUY_PERCENTAGE,
        maxSoldPercentage = MAX_SOLD_PERCENTAGE,
        maxExtremeBuyPercentage = MAX_EXTREME_BUY_PERCENTAGE,
        maxExtremeSoldPercentage = MAX_EXTREME_SOLD_PERCENTAGE,
        minFreezePeriodInDays = MIN_FREEZE_PERIOD_IN_DAYS
    )
) : RecommendationAlgorithm() {
    override fun getRecommendation(
        stockPrice: StockPrice, currencyRateInSek: Double, commissionFee: Double, stockEvents: Set<StockEvent>,
        timeInMillis: Long,
    ): Recommendation? {
        return getRecommendation(
            stockPrice.stockSymbol, stockPrice.currency,
            currencyRateInSek, stockEvents, stockPrice.price, commissionFee, timeInMillis
        )
    }

    private fun getRecommendation(
        stockName: String, currency: Currency, rateInSek: Double,
        stockEvents: Set<StockEvent>,
        currentStockPriceInStockCurrency: Double, commissionFeeInSek: Double,
        timeInMillis: Long,
    ): Recommendation? {
        val orders = stockEvents.filter { stockEvent -> stockEvent.stockOrder != null }.map { it.stockOrder!! }
        val stockSplits = stockEvents.filter { stockEvent -> stockEvent.stockSplit != null }.map { it.stockSplit!! }
        if (orders.isEmpty()) {
            // Dummy recommendation to mimic first buy
            return Recommendation(
                BuyStockCommand(
                    timeInMillis, currency, stockName, currentStockPriceInStockCurrency,
                    1, commissionFeeInSek
                )
            )
        }
        val calculator = PortfolioCalculator(stockEvents, stockName, rateInSek)
        val portfolioState = calculator.calculate()
        val grossQuantity = portfolioState.grossQuantity
        val soldQuantity = portfolioState.soldQuantity
        val accumulatedCostInSek = portfolioState.accumulatedCostInSek
        val currentStockPriceInSek = currentStockPriceInStockCurrency * rateInSek

        val sortedOrders = orders.sortedBy { it.dateInMillis }
        if (grossQuantity - soldQuantity == 0 && isCurrentStockBelowLastSale(
                sortedOrders.last(),
                currentStockPriceInStockCurrency
            )
        ) {
            val buyQuantity: Int =
                ((configuration.firstPurchaseOrderInSek - commissionFeeInSek) / currentStockPriceInSek).toInt()
            val netStockPriceInStockCurrency =
                ((commissionFeeInSek / rateInSek) + currentStockPriceInStockCurrency * buyQuantity) / buyQuantity
            if (buyQuantity > 0 && isCurrentStockBelowLastSale(sortedOrders.last(), netStockPriceInStockCurrency)) {
                return Recommendation(
                    BuyStockCommand(
                        timeInMillis, currency, stockName, currentStockPriceInStockCurrency,
                        buyQuantity, commissionFeeInSek
                    )
                )
            }
        }
        val netQuantity = grossQuantity - soldQuantity
        val averageCostInSek = accumulatedCostInSek / grossQuantity
        val costToExcludeInSek = averageCostInSek * soldQuantity

        val totalPricePerStockInSek = (accumulatedCostInSek - costToExcludeInSek) / netQuantity
        val totalPricePerStockInStockCurrency = totalPricePerStockInSek / rateInSek
        var tradeQuantity =
            (netQuantity / 10).coerceAtMost((configuration.maxPurchaseOrderInSek / currentStockPriceInSek).toInt())
        var recommendation: Recommendation? = null
        var isOkToContinue = true
        var daysSinceLastBuy = Long.MAX_VALUE
        var daysSinceLastSale = Long.MAX_VALUE
        if (orders.last().orderAction == "Buy") {
            daysSinceLastBuy = TimeUnit.MILLISECONDS.toDays(timeInMillis - orders.last().dateInMillis)
        } else {
            daysSinceLastSale = TimeUnit.MILLISECONDS.toDays(timeInMillis - orders.last().dateInMillis)
        }
        while (isOkToContinue) {
            val pricePerStockAfterBuyInStockCurrency = ((netQuantity * averageCostInSek +
                    tradeQuantity * currentStockPriceInSek + commissionFeeInSek) /
                    (netQuantity + tradeQuantity)) / rateInSek
            if (isCurrentStockPriceHighEnoughToSell(
                    tradeQuantity, currentStockPriceInStockCurrency,
                    totalPricePerStockInStockCurrency, commissionFeeInSek / rateInSek
                ) &&
                hasEnoughDaysElapsed(daysSinceLastSale)
            ) {
                if (isNotOverSoldForMediumStockPriceIncrease(tradeQuantity, soldQuantity, grossQuantity) &&
                    tradeWithinMaxPriceLimit(tradeQuantity, currentStockPriceInSek)
                ) {
                    isOkToContinue = true
                    recommendation = Recommendation(
                        SellStockCommand(
                            timeInMillis, currency, stockName, currentStockPriceInStockCurrency,
                            tradeQuantity, commissionFeeInSek
                        )
                    )
                    tradeQuantity += 1
                } else {
                    if (isNotOverSoldForHighStockPriceIncrease(tradeQuantity, soldQuantity, grossQuantity) &&
                        tradeWithinMaxPriceLimit(tradeQuantity, currentStockPriceInSek) &&
                        hasEnoughDaysElapsed(daysSinceLastSale)
                    ) {
                        isOkToContinue = true
                        recommendation = Recommendation(
                            SellStockCommand(
                                timeInMillis, currency, stockName, currentStockPriceInStockCurrency,
                                tradeQuantity, commissionFeeInSek
                            )
                        )
                        tradeQuantity += 1
                    } else {
                        return recommendation
                    }
                }
            } else if (currentStockPriceLowEnoughForBuy(
                    currentStockPriceInStockCurrency,
                    pricePerStockAfterBuyInStockCurrency
                ) &&
                pricePerStockAfterBuyInStockCurrency < totalPricePerStockInStockCurrency &&
                hasEnoughDaysElapsed(daysSinceLastBuy)
            ) {
                if (isNotOverBoughtForMediumStockPriceDecrease(tradeQuantity, soldQuantity, grossQuantity) &&
                    tradeWithinMaxPriceLimit(tradeQuantity, currentStockPriceInSek)
                ) {
                    isOkToContinue = true
                    recommendation = Recommendation(
                        BuyStockCommand(
                            timeInMillis, currency, stockName, currentStockPriceInStockCurrency,
                            tradeQuantity, commissionFeeInSek
                        )
                    )
                    tradeQuantity += 1
                } else {
                    if (isNotOverBoughtForHighStockPriceDecrease(tradeQuantity, soldQuantity, grossQuantity) &&
                        tradeWithinMaxPriceLimit(tradeQuantity, currentStockPriceInSek)
                    ) {
                        isOkToContinue = true
                        recommendation = Recommendation(
                            BuyStockCommand(
                                timeInMillis, currency, stockName, currentStockPriceInStockCurrency,
                                tradeQuantity, commissionFeeInSek
                            )
                        )
                        tradeQuantity += 1
                    } else {
                        isOkToContinue = false
                    }
                }
            } else {
                isOkToContinue = false
            }
        }
        return recommendation
    }

    private fun tradeWithinMaxPriceLimit(tradeQuantity: Int, rateInSek: Double) =
        (tradeQuantity * rateInSek) <= configuration.maxPurchaseOrderInSek

    private fun hasEnoughDaysElapsed(toDays: Long): Boolean {
        return toDays >= configuration.minFreezePeriodInDays
    }

    private fun isCurrentStockBelowLastSale(lastSaleOrder: StockOrder, currentStockPrice: Double) =
        lastSaleOrder.pricePerStock >= currentStockPrice * (1 + configuration.normalDiffPercentage)

    private fun isCurrentStockPriceHighEnoughToSell(
        tradeQuantity: Int, stockPrice: Double, totalPricePerStockInStockCurrency: Double, commissionFee: Double,
    ) =
        tradeQuantity * stockPrice > (tradeQuantity * ((1 + configuration.normalDiffPercentage) * totalPricePerStockInStockCurrency) + commissionFee)

    private fun isNotOverBoughtForHighStockPriceDecrease(tradeQuantity: Int, soldQuantity: Int, grossQuantity: Int) =
        isNotOverBought(tradeQuantity, soldQuantity, grossQuantity, configuration.maxExtremeBuyPercentage)

    private fun isNotOverBoughtForMediumStockPriceDecrease(tradeQuantity: Int, soldQuantity: Int, grossQuantity: Int) =
        isNotOverBought(tradeQuantity, soldQuantity, grossQuantity, configuration.maxBuyPercentage)

    private fun isNotOverSoldForHighStockPriceIncrease(tradeQuantity: Int, soldQuantity: Int, grossQuantity: Int) =
        isNotOverSold(tradeQuantity, soldQuantity, grossQuantity, configuration.maxExtremeSoldPercentage)

    private fun isNotOverSoldForMediumStockPriceIncrease(tradeQuantity: Int, soldQuantity: Int, grossQuantity: Int) =
        isNotOverSold(tradeQuantity, soldQuantity, grossQuantity, configuration.maxSoldPercentage)

    private fun isNotOverBought(tradeQuantity: Int, soldQuantity: Int, grossQuantity: Int, threshold: Double) =
        tradeQuantity > 0 && (soldQuantity + tradeQuantity) <= grossQuantity * threshold

    private fun isNotOverSold(tradeQuantity: Int, soldQuantity: Int, grossQuantity: Int, threshold: Double) =
        tradeQuantity > 0 && (soldQuantity + tradeQuantity) <= grossQuantity * threshold

    private fun currentStockPriceLowEnoughForBuy(stockPrice: Double, predictedStockPriceAfterBuy: Double) =
        stockPrice < (1 - configuration.normalDiffPercentage) * predictedStockPriceAfterBuy

    companion object {
        const val DEFAULT_FIRST_PURCHASE_ORDER_IN_SEK: Double = 3000.0
        const val MAX_PURCHASE_ORDER_IN_SEK: Double = 1000.0
        const val NORMAL_DIFF_PERCENTAGE: Double = .20
        const val MAX_BUY_PERCENTAGE: Double = .10
        const val MAX_SOLD_PERCENTAGE: Double = .10
        const val MAX_EXTREME_BUY_PERCENTAGE: Double = .20
        const val MAX_EXTREME_SOLD_PERCENTAGE: Double = .75
        const val MIN_FREEZE_PERIOD_IN_DAYS: Long = 7
    }

}

