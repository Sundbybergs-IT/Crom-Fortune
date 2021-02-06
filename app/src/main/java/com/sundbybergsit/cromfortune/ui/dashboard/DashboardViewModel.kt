package com.sundbybergsit.cromfortune.ui.dashboard

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.stocks.StockOrderRepositoryImpl
import com.sundbybergsit.cromfortune.ui.home.*
import kotlinx.coroutines.launch
import java.time.Instant

private const val COMMISSION_FEE = 39.0

class DashboardViewModel : ViewModel() {

    companion object {

        const val TAG = "DashboardViewModel"

    }

    private val _recommendationViewState = MutableLiveData<RecommendationViewState>().apply {
        value = RecommendationViewState.NONE
    }
    private var lastUpdated: Instant = Instant.ofEpochMilli(0L)

    val recommendationViewState: LiveData<RecommendationViewState> = _recommendationViewState

    private val _score = MutableLiveData<String>().apply {
        value = ""
    }
    val score: LiveData<String> = _score

    fun refresh(context: Context, timestamp: Instant, stockPrices: Set<StockPrice>) {
        Log.i(TAG, "refresh(${stockPrices})")
        if (timestamp.isAfter(lastUpdated)) {
            lastUpdated = timestamp
            viewModelScope.launch {
                val stockOrderRepository = StockOrderRepositoryImpl(context)
                for (stockPrice in stockPrices) {
                    val recommendation = CromFortuneV1RecommendationAlgorithm(context)
                            .getRecommendation(stockPrice, COMMISSION_FEE, stockOrderRepository.list(stockPrice.stockSymbol))
                    _recommendationViewState.postValue(when (recommendation) {
                        is Recommendation -> RecommendationViewState.OK(recommendation)
                        else -> RecommendationViewState.NONE
                    })
                }
                val repository = StockOrderRepositoryImpl(context)
                val latestScore = CromFortuneV1AlgorithmConformanceScoreCalculator().getScore(recommendationAlgorithm =
                CromFortuneV1RecommendationAlgorithm(context), orders = stocks(repository).toSet(),
                        currencyRateRepository = CurrencyRateRepository
                )
                _score.postValue(context.getString(R.string.dashboard_croms_will_message, latestScore.score.toString()))
            }
        } else {
            Log.w(TAG, "Ignoring old data...")
        }
    }

    private fun stocks(repository: StockOrderRepositoryImpl): List<StockOrder> {
        val stocks = mutableListOf<StockOrder>()
        for (stockName in repository.listOfStockNames()) {
            for (entry in repository.list(stockName)) {
                stocks.add(entry)
            }
        }
        return stocks
    }

    sealed class RecommendationViewState {
        object NONE : RecommendationViewState()
        data class OK(val recommendation: Recommendation) : RecommendationViewState()
    }

}
