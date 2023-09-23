package com.sundbybergsit.cromfortune.main.ui.dashboard

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sundbybergsit.cromfortune.algorithm.cromfortunev1.CromFortuneV1AlgorithmConformanceScoreCalculator
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockEventApi
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.main.R
import com.sundbybergsit.cromfortune.main.TAG
import com.sundbybergsit.cromfortune.main.crom.CromFortuneV1RecommendationAlgorithm
import com.sundbybergsit.cromfortune.main.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.main.stocks.StockEventRepository
import kotlinx.coroutines.launch
import java.time.Instant

class DashboardViewModel : ViewModel() {

    private var lastUpdated: Instant = Instant.ofEpochMilli(0L)

    private val _score : MutableState<String> = mutableStateOf("")

    val score: State<String> = _score

    fun refresh(context: Context, timestamp: Instant, stockPrices: Set<StockPrice>) {
        Log.i(TAG, "refresh(${stockPrices})")
        if (timestamp.isAfter(lastUpdated)) {
            lastUpdated = timestamp
            viewModelScope.launch {
                val repository = StockEventRepository(context)
                val latestScore = CromFortuneV1AlgorithmConformanceScoreCalculator()
                    .getScore(recommendationAlgorithm =
                CromFortuneV1RecommendationAlgorithm(context), stockEvents = events(repository).toSet(),
                        currencyRateApi = CurrencyRateRepository
                )
                _score.value = context.resources.getQuantityString(R.plurals.dashboard_croms_will_message,
                        latestScore.score, latestScore.score)
            }
        } else {
            Log.w(TAG, "Ignoring old data...")
        }
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
