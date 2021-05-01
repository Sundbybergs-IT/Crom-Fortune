package com.sundbybergsit.cromfortune.ui.dashboard

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.crom.CromFortuneV1AlgorithmConformanceScoreCalculator
import com.sundbybergsit.cromfortune.crom.CromFortuneV1RecommendationAlgorithm
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.stocks.StockOrder
import com.sundbybergsit.cromfortune.stocks.StockOrderRepositoryImpl
import com.sundbybergsit.cromfortune.stocks.StockPrice
import kotlinx.coroutines.launch
import java.time.Instant

class DashboardViewModel : ViewModel() {

    companion object {

        const val TAG = "DashboardViewModel"

    }

    private var lastUpdated: Instant = Instant.ofEpochMilli(0L)

    private val _score = MutableLiveData<String>().apply {
        value = ""
    }

    val score: LiveData<String> = _score

    fun refresh(context: Context, timestamp: Instant, stockPrices: Set<StockPrice>) {
        Log.i(TAG, "refresh(${stockPrices})")
        if (timestamp.isAfter(lastUpdated)) {
            lastUpdated = timestamp
            viewModelScope.launch {
                val repository = StockOrderRepositoryImpl(context)
                val latestScore = CromFortuneV1AlgorithmConformanceScoreCalculator().getScore(recommendationAlgorithm =
                CromFortuneV1RecommendationAlgorithm(context), orders = stocks(repository).toSet(),
                        currencyRateRepository = CurrencyRateRepository
                )
                _score.postValue(context.resources.getQuantityString(R.plurals.dashboard_croms_will_message,
                        latestScore.score, latestScore.score))
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

}
