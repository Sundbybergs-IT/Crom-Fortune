package com.sundbybergsit.cromfortune

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sundbybergsit.cromfortune.currencies.CurrencyRate
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.stocks.StockOrderRepositoryImpl
import com.sundbybergsit.cromfortune.stocks.StockPrice
import com.sundbybergsit.cromfortune.stocks.StockPriceRepository
import com.sundbybergsit.cromfortune.ui.home.BuyStockCommand
import com.sundbybergsit.cromfortune.ui.home.CromFortuneV1RecommendationAlgorithm
import com.sundbybergsit.cromfortune.ui.home.Recommendation
import com.sundbybergsit.cromfortune.ui.home.SellStockCommand
import com.sundbybergsit.cromfortune.ui.notifications.NotificationMessage
import com.sundbybergsit.cromfortune.ui.notifications.NotificationUtil
import com.sundbybergsit.cromfortune.ui.notifications.NotificationsRepositoryImpl
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import yahoofinance.Stock
import yahoofinance.YahooFinance
import java.util.*

@Suppress("BlockingMethodInNonBlockingContext")
open class StockDataRetrievalCoroutineWorker(val context: Context, workerParameters: WorkerParameters) :
        CoroutineWorker(context, workerParameters) {

    companion object {

        const val TAG = "StockRetrievalCoroutineWorker"
        private const val COMMISSION_FEE = 39.0

    }

    override suspend fun doWork(): Result = coroutineScope {
        Log.i(TAG, "doWork()")
        try {
            val asyncWork =
                    async {
                        val currencyRates: MutableSet<CurrencyRate> = mutableSetOf()
                        currencyRates.add(CurrencyRate("SEK", 1.0))
                        for (currency in arrayOf("CAD", "EUR", "NOK", "USD")) {
                            currencyRates.add(CurrencyRate(currency, getRateInSek(currency)))
                        }
                        CurrencyRateRepository.add(currencyRates)
                        val stocks: Map<String, Stock> = YahooFinance.get(StockPrice.SYMBOLS.map { pair -> pair.first }
                                .toTypedArray())
                        val stockPrices = mutableSetOf<StockPrice>()
                        for (triple in StockPrice.SYMBOLS.iterator()) {
                            val stockSymbol = triple.first
                            val quote = (stocks[stockSymbol] ?: error("")).getQuote(true)
                            val currency = triple.third
                            val stockPrice = StockPrice(stockSymbol = stockSymbol, currency = Currency.getInstance(currency),
                                    price = quote.price.toDouble().roundTo(3))
                            val recommendation = CromFortuneV1RecommendationAlgorithm(context)
                                    .getRecommendation(stockPrice, currencyRates.find {
                                        currencyRate -> currencyRate.iso4217CurrencySymbol == stockPrice.currency.currencyCode }!!.rateInSek,
                                            COMMISSION_FEE, StockOrderRepositoryImpl(context).list(stockSymbol))
                            if (recommendation != null) {
                                notifyRecommendation(recommendation)
                            }
                            stockPrices.add(stockPrice)
                        }
                        StockPriceRepository.put(stockPrices)
                    }
            asyncWork.await()
            Result.success()
        } catch (error: Throwable) {
            Result.failure()
        }
    }

    open fun getRateInSek(currency: String) = YahooFinance.getFx("${currency}SEK=X").price.toDouble()

    private fun notifyRecommendation(recommendation: Recommendation) {
        val notification = NotificationMessage(System.currentTimeMillis(),
                recommendation.command.toString())
        // TODO: Move repository logic
        val notificationsRepository = NotificationsRepositoryImpl(context)
        notificationsRepository.add(notification)
        val shortText: String =
                when (recommendation.command) {
                    is BuyStockCommand -> context.getString(R.string.action_stock_buy)
                    is SellStockCommand -> context.getString(R.string.action_stock_sell)
                    else -> ""
                }
        NotificationUtil.doPostRegularNotification(context,
                context.getString(R.string.notification_recommendation_title),
                shortText,
                "${context.getString(R.string.notification_recommendation_body)} ${notification.message}")
    }

}
