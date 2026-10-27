package com.sundbybergsit.cromfortune.main

import android.content.Context
import com.sundbybergsit.cromfortune.domain.AssetCatalog
import com.sundbybergsit.cromfortune.main.settings.StockRetrievalSettings
import com.sundbybergsit.cromfortune.main.stocks.StockPriceRepository
import java.time.Duration
import java.time.Instant

object AssetRefreshCoordinator {

    fun shouldRefreshAutomatically(context: Context, now: Instant = Instant.now()): Boolean {
        val refreshInterval = StockRetrievalSettings(context).timeInterval.value.refreshInterval
        val refreshMaxAge = Duration.ofMinutes(refreshInterval.minutes.toLong())
        val requiredAssetIds = AssetDataRetrievalCoroutineWorker.assetsToRefresh(context).mapTo(mutableSetOf()) { it.id }
        val statusesById = StockPriceRepository.assetPricesStateFlow.value.statuses
            .associateBy { it.assetPrice.assetId }
        return requiredAssetIds.any { assetId ->
            val status = statusesById[assetId] ?: return@any true
            status.isStale || Duration.between(status.lastUpdatedAt, now) >= refreshMaxAge
        }
    }

    @Synchronized
    fun refresh(
        context: Context,
        portfolioRepository: PortfolioRepository,
        trigger: RefreshTrigger,
        marketDataClient: MarketDataClient = YahooMarketDataClient
    ): RefreshOutcome {
        AssetRefreshStatusRepository.recordAttempt(trigger)
        return try {
            val assets = when (trigger) {
                RefreshTrigger.MANUAL -> AssetCatalog.activeAssets
                RefreshTrigger.BACKGROUND -> AssetDataRetrievalCoroutineWorker.assetsToRefresh(context)
            }
            val outcome = AssetDataRetrievalCoroutineWorker.refreshFromYahoo(
                context = context,
                portfolioRepository = portfolioRepository,
                marketDataClient = marketDataClient,
                assets = assets,
                notificationsAllowed = trigger == RefreshTrigger.BACKGROUND &&
                    AssetDataRetrievalCoroutineWorker.isWithinNotificationWindow(context)
            )
            if (outcome.successfulAssets == 0 && outcome.failedAssets > 0) {
                throw AssetRefreshException("No market prices could be retrieved")
            }
            AssetRefreshStatusRepository.recordSuccess(
                successfulAssets = outcome.successfulAssets,
                failedAssets = outcome.failedAssets
            )
            outcome
        } catch (error: Throwable) {
            AssetRefreshStatusRepository.recordFailure(error)
            throw error
        }
    }
}

data class RefreshOutcome(val successfulAssets: Int, val failedAssets: Int)

class AssetRefreshException(message: String, cause: Throwable? = null) : Exception(message, cause)
