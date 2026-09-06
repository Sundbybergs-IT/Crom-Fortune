package com.sundbybergsit.cromfortune.main.ui.dashboard

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sundbybergsit.cromfortune.algorithm.cromfortunev1.CromFortuneV1AlgorithmConformanceScoreCalculator
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockEventApi
import com.sundbybergsit.cromfortune.main.PortfolioRepository
import com.sundbybergsit.cromfortune.main.R
import com.sundbybergsit.cromfortune.main.TAG
import com.sundbybergsit.cromfortune.main.crom.CromFortuneV1RecommendationAlgorithm
import com.sundbybergsit.cromfortune.main.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.main.stocks.AssetEventRepository
import com.sundbybergsit.cromfortune.main.stocks.StockEventRepository
import com.sundbybergsit.cromfortune.main.stocks.StockPriceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

class DashboardViewModel : ViewModel() {

    private var lastUpdated: Instant = Instant.ofEpochMilli(0L)

    private val _scoreStateFlow: MutableStateFlow<String> = MutableStateFlow("")

    val scoreStateFlow: StateFlow<String> = _scoreStateFlow
    private val _portfolioSummaryStateFlow = MutableStateFlow("")
    val portfolioSummaryStateFlow: StateFlow<String> = _portfolioSummaryStateFlow

    fun refresh(context: Context, timestamp: Instant) {
        Log.i(TAG, "refresh($timestamp)")
        if (timestamp.isAfter(lastUpdated)) {
            lastUpdated = timestamp
            viewModelScope.launch {
                val repository =
                    StockEventRepository(context = context, portfolioName = PortfolioRepository.DEFAULT_PORTFOLIO_NAME)
                val recommendationAlgorithm = CromFortuneV1RecommendationAlgorithm()
                val latestScore = CromFortuneV1AlgorithmConformanceScoreCalculator()
                    .getScore(
                        recommendationAlgorithm = recommendationAlgorithm,
                        stockEvents = events(repository).filter { event ->
                            event.stockOrder?.let { order -> recommendationAlgorithm.supports(order.assetType) } ?: true
                        }.toSet(),
                        currencyRateApi = CurrencyRateRepository
                    )
                _scoreStateFlow.value = context.resources.getQuantityString(
                    R.plurals.dashboard_croms_will_message,
                    latestScore.score, latestScore.score
                )
                updatePortfolioSummary(context)
            }
        } else {
            Log.w(TAG, "Ignoring old data...")
        }
    }

    private fun updatePortfolioSummary(context: Context) {
        val repository = AssetEventRepository(context, PortfolioRepository.DEFAULT_PORTFOLIO_NAME)
        var totalInSek = BigDecimal.ZERO
        var cryptoHoldings = 0
        repository.assetIds().forEach { assetId ->
            val events = repository.list(assetId)
            val transaction = events.firstNotNullOfOrNull { event -> event.transaction } ?: return@forEach
            if (transaction.assetType == AssetType.CRYPTO) cryptoHoldings++
            val price = StockPriceRepository.getAssetPrice(assetId)?.price ?: return@forEach
            val rate = CurrencyRateRepository.currencyRates.value
                .find { it.iso4217CurrencySymbol == transaction.quoteCurrencyCode }
                ?.rateInSek?.toBigDecimal() ?: BigDecimal.ONE
            totalInSek += repository.currentQuantity(assetId).multiply(price).multiply(rate)
        }
        _portfolioSummaryStateFlow.value =
            "${repository.assetIds().size} holdings · $cryptoHoldings crypto · " +
                "${totalInSek.setScale(2, RoundingMode.HALF_UP).toPlainString()} SEK"
    }

    private fun events(repository: StockEventApi): List<StockEvent> {
        val events = mutableListOf<StockEvent>()
        for (stockName in repository.listOfStockNames()) {
            for (entry in repository.list(stockName)) {
                events.add(entry)
            }
        }
        return events
    }

}
