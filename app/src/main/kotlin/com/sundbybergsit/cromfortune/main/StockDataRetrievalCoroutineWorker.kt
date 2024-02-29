package com.sundbybergsit.cromfortune.main

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sundbybergsit.cromfortune.algorithm.api.Recommendation
import com.sundbybergsit.cromfortune.algorithm.core.BuyStockCommand
import com.sundbybergsit.cromfortune.algorithm.core.SellStockCommand
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.currencies.CurrencyRate
import com.sundbybergsit.cromfortune.domain.notifications.NotificationMessage
import com.sundbybergsit.cromfortune.domain.util.roundTo
import com.sundbybergsit.cromfortune.main.crom.CromFortuneV1RecommendationAlgorithm
import com.sundbybergsit.cromfortune.main.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.main.notifications.NotificationUtil
import com.sundbybergsit.cromfortune.main.notifications.NotificationsRepositoryImpl
import com.sundbybergsit.cromfortune.main.settings.StockMuteSettingsRepository
import com.sundbybergsit.cromfortune.main.settings.StockRetrievalSettings
import com.sundbybergsit.cromfortune.main.stocks.StockEventRepository
import com.sundbybergsit.cromfortune.main.stocks.StockPriceRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import yahoofinance.StockV2
import yahoofinance.get
import yahoofinance.getFxHax
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.util.Currency
import kotlin.math.roundToInt

class StockDataRetrievalCoroutineWorker(private val context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {

    companion object {

        const val TAG = "StockRetrievalCoroutineWorker"
        const val COMMISSION_FEE = 39.0

        fun refreshFromYahoo(context: Context, portfolioRepository: PortfolioRepository, onFinished: () -> Unit) {
            val currencyRates: MutableSet<CurrencyRate> = mutableSetOf()
            currencyRates.add(CurrencyRate("SEK", 1.0))
            for (currency in arrayOf("CAD", "EUR", "NOK", "USD")) {
                currencyRates.add(CurrencyRate(currency, getRateInSek(currency)))
            }
            CurrencyRateRepository.addAll(currencyRates)
            val stocks: Map<String, StockV2> =
                get(StockPrice.SYMBOLS.map { pair -> pair.first }
                    .toTypedArray())
            val stockPrices = mutableSetOf<StockPrice>()
            for (triple in StockPrice.SYMBOLS.iterator()) {
                val stockSymbol = triple.first
                val stockV2 =
                    stocks[stockSymbol] ?: throw IllegalStateException("Cannot find stock symbol: $stockSymbol")
                val quote = stockV2.getQuote(true)
                val currency = triple.third
                val stockPrice = StockPrice(
                    stockSymbol = stockSymbol, currency = Currency.getInstance(currency),
                    price = quote.price.toDouble().roundTo(3)
                )
                val allPortfolioNamesState = portfolioRepository.portfolioNamesStateFlow.value
                for (portfolioName in allPortfolioNamesState.filterNot { name -> name == PortfolioRepository.CROM_PORTFOLIO_NAME }) {
                    val stockEvents = StockEventRepository(context, portfolioName).list(stockSymbol)
                    val isStockMuted = StockMuteSettingsRepository.isMuted(stockSymbol)
                    if (isStockMuted) {
                        Log.i(
                            TAG,
                            "Skipping recommendation for portfolio [${portfolioName}] for stock [${stockSymbol}] as it has been muted."
                        )
                    } else if (stockEvents.isNotEmpty()) {
                        val recommendation = CromFortuneV1RecommendationAlgorithm(context)
                            .getRecommendation(
                                stockPrice = stockPrice,
                                currencyRateInSek = currencyRates.find { currencyRate -> currencyRate.iso4217CurrencySymbol == stockPrice.currency.currencyCode }!!.rateInSek,
                                commissionFee = COMMISSION_FEE,
                                stockEvents = stockEvents,
                                timeInMillis = System.currentTimeMillis()
                            )
                        if (recommendation != null) {
                            notifyRecommendation(
                                context = context,
                                portfolioName = portfolioName,
                                recommendation = recommendation
                            )
                        }
                    }
                }
                stockPrices.add(stockPrice)
            }
            (context.applicationContext as CromFortuneApp).lastRefreshed = Instant.now()
            StockPriceRepository.put(stockPrices)
            onFinished()
        }

        private fun notifyRecommendation(context: Context, recommendation: Recommendation, portfolioName: String) {
            val message = when (recommendation.command) {
                is BuyStockCommand -> {
                    context.getString(
                        R.string.notification_recommendation_body_buy,
                        portfolioName,
                        (recommendation.command as BuyStockCommand).quantity,
                        (recommendation.command as BuyStockCommand).name,
                        (recommendation.command as BuyStockCommand).pricePerStock.roundTo(3).toString(),
                        (recommendation.command as BuyStockCommand).currency.currencyCode,
                        (recommendation.command as BuyStockCommand).commissionFee.roundToInt()
                    )
                }

                is SellStockCommand -> {
                    context.getString(
                        R.string.notification_recommendation_body_sell,
                        portfolioName,
                        (recommendation.command as SellStockCommand).quantity,
                        (recommendation.command as SellStockCommand).name,
                        (recommendation.command as SellStockCommand).pricePerStock.roundTo(3).toString(),
                        (recommendation.command as SellStockCommand).currency.currencyCode,
                        (recommendation.command as SellStockCommand).commissionFee.roundToInt()
                    )
                }

                else -> {
                    ""
                }
            }

            val notification = NotificationMessage(System.currentTimeMillis(), message)

            // TODO: Move repository logic
            val notificationsRepository = NotificationsRepositoryImpl(context)
            notificationsRepository.add(notification)
            val shortText: String =
                when (recommendation.command) {
                    is BuyStockCommand -> context.getString(
                        R.string.generic_urge_buy,
                        recommendation.command.stockSymbol()
                    )

                    is SellStockCommand -> context.getString(
                        R.string.generic_urge_sell,
                        recommendation.command.stockSymbol()
                    )

                    else -> ""
                }
            NotificationUtil.doPostRegularNotification(
                context,
                context.getString(R.string.notification_recommendation_title),
                shortText,
                notification.message
            )
        }

        private fun getRateInSek(currency: String) = getFxHax("${currency}SEK=X").price.toDouble()

    }

    override suspend fun doWork(): Result = coroutineScope {
        Log.i(TAG, "doWork()")
        try {
            val asyncWork =
                async {
                    val timeInterval = StockRetrievalSettings(context).timeInterval.value
                    val currentTime = LocalTime.now()
                    val currentDayOfWeek = LocalDate.now().dayOfWeek
                    val fromTime = LocalTime.of(timeInterval.fromTimeHours, timeInterval.fromTimeMinutes)
                    val toTime = LocalTime.of(timeInterval.toTimeHours, timeInterval.toTimeMinutes)
                    when {
                        isRefreshRequired() -> {
                            Log.i(TAG, "Initial retrieval of data.")
                            refreshFromYahoo(
                                context = context,
                                portfolioRepository = PortfolioRepository,
                                onFinished = { })
                        }

                        timeInterval.weekDays.isWithinConfiguredTimeInterval(
                            currentDayOfWeek, currentTime,
                            fromTime, toTime
                        ) -> {
                            Log.i(TAG, "Within configured time interval. Will therefore retrieve data.")
                            refreshFromYahoo(
                                context = context,
                                portfolioRepository = PortfolioRepository,
                                onFinished = { })
                        }

                        else -> {
                            Log.i(TAG, "User has disabled stock retrieval at this time. Will not retrieve data.")
                        }
                    }
                }
            asyncWork.await()
            Result.success()
        } catch (error: Throwable) {
            Result.failure()
        }
    }

    private fun isRefreshRequired(): Boolean {
        return StockPriceRepository.stockPricesStateFlow.value.stockPrices.isEmpty()
    }

}
