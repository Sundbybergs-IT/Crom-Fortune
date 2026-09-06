package com.sundbybergsit.cromfortune.main

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sundbybergsit.cromfortune.algorithm.api.Recommendation
import com.sundbybergsit.cromfortune.algorithm.core.BuyStockCommand
import com.sundbybergsit.cromfortune.algorithm.core.SellStockCommand
import com.sundbybergsit.cromfortune.domain.AssetCatalog
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
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.roundToInt

class StockDataRetrievalCoroutineWorker(
    private val context: Context,
    workerParameters: WorkerParameters,
    private val marketDataClient: MarketDataClient = YahooMarketDataClient
) :
    CoroutineWorker(context, workerParameters) {

    companion object {

        const val TAG = "StockRetrievalCoroutineWorker"
        const val COMMISSION_FEE = 39.0

        fun refreshFromYahoo(
            context: Context,
            portfolioRepository: PortfolioRepository,
            onFinished: () -> Unit,
            marketDataClient: MarketDataClient = YahooMarketDataClient
        ) {
            val notificationsAllowed = isWithinNotificationWindow(context)
            val currencyRates: MutableSet<CurrencyRate> = mutableSetOf()
            currencyRates.add(CurrencyRate("SEK", 1.0))
            val quoteCurrencies = AssetCatalog.assets.map { asset -> asset.quoteCurrency }.distinct()
            for (currency in quoteCurrencies.filterNot { it.currencyCode == "SEK" }) {
                currencyRates.add(CurrencyRate(currency.currencyCode, marketDataClient.getRateInSek(currency)))
            }
            CurrencyRateRepository.addAll(currencyRates)
            val assetPricesById = marketDataClient.getPrices(AssetCatalog.assets)
            for (asset in AssetCatalog.stocks) {
                val assetPrice = assetPricesById[asset.id]
                if (assetPrice == null) {
                    Log.e(TAG, "Skipping ${asset.symbol} as it cannot be found in the market-data API.")
                } else {
                    val stockPrice = StockPrice(
                        stockSymbol = asset.symbol,
                        currency = assetPrice.currency,
                        price = assetPrice.price.roundTo(3)
                    )
                    val allPortfolioNamesState = portfolioRepository.portfolioNamesStateFlow.value
                    for (portfolioName in allPortfolioNamesState.filterNot { name -> name == PortfolioRepository.CROM_PORTFOLIO_NAME }) {
                        val stockEvents = StockEventRepository(context, portfolioName).list(asset.symbol)
                        val isStockMuted = StockMuteSettingsRepository.isMuted(asset.symbol)
                        if (isStockMuted) {
                            Log.i(
                                TAG,
                                "Skipping recommendation for portfolio [${portfolioName}] for stock [${asset.symbol}] as it has been muted."
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
                                if (notificationsAllowed) {
                                    notifyRecommendation(
                                        context = context,
                                        portfolioName = portfolioName,
                                        recommendation = recommendation
                                    )
                                } else {
                                    Log.i(
                                        TAG,
                                            "Skipping recommendation notification for portfolio [$portfolioName] " +
                                            "for stock [${asset.symbol}] outside configured time interval."
                                    )
                                }
                            }
                        }
                    }
                }
            }
            (context.applicationContext as CromFortuneApp).lastRefreshed = Instant.now()
            StockPriceRepository.putAssetPrices(assetPricesById.values.toSet())
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

        internal fun isWithinNotificationWindow(
            context: Context,
            currentDayOfWeek: DayOfWeek = LocalDate.now().dayOfWeek,
            currentTime: LocalTime = LocalTime.now()
        ): Boolean {
            val timeInterval = StockRetrievalSettings(context).timeInterval.value
            val fromTime = LocalTime.of(timeInterval.fromTimeHours, timeInterval.fromTimeMinutes)
            val toTime = LocalTime.of(timeInterval.toTimeHours, timeInterval.toTimeMinutes)
            return timeInterval.weekDays.isWithinConfiguredTimeInterval(
                currentDayOfWeek = currentDayOfWeek,
                currentTime = currentTime,
                fromTime = fromTime,
                toTime = toTime
            )
        }

    }

    override suspend fun doWork(): Result {
        Log.i(TAG, "doWork()")
        return try {
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
                        onFinished = { },
                        marketDataClient = marketDataClient
                    )
                }

                timeInterval.weekDays.isWithinConfiguredTimeInterval(
                    currentDayOfWeek, currentTime,
                    fromTime, toTime
                ) -> {
                    Log.i(TAG, "Within configured time interval. Will therefore retrieve data.")
                    refreshFromYahoo(
                        context = context,
                        portfolioRepository = PortfolioRepository,
                        onFinished = { },
                        marketDataClient = marketDataClient
                    )
                }

                else -> {
                    Log.i(TAG, "User has disabled stock retrieval at this time. Will not retrieve data.")
                }
            }
            Result.success()
        } catch (error: Throwable) {
            Result.failure()
        }
    }

    private fun isRefreshRequired(): Boolean {
        return StockPriceRepository.assetPricesStateFlow.value.assetPrices.isEmpty()
    }

}
