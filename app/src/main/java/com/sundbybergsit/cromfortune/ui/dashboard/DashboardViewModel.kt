package com.sundbybergsit.cromfortune.ui.dashboard

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sundbybergsit.cromfortune.ui.home.*
import kotlinx.coroutines.launch

private const val COMMISSION_FEE = 39.0

class DashboardViewModel : ViewModel() {

    private val _recommendationViewState = MutableLiveData<RecommendationViewState>().apply {
        value = RecommendationViewState.NONE
    }

    val recommendationViewState: LiveData<RecommendationViewState> = _recommendationViewState

    private val _score = MutableLiveData<String>().apply {
        value = ""
    }
    val score: LiveData<String> = _score

    fun refresh(context: Context, stockPrice: StockPrice) {
        viewModelScope.launch {
            val stockOrderRepository = StockOrderRepositoryImpl(context)
            val recommendation = CromFortuneV1RecommendationAlgorithm(context, stockOrderRepository)
                    .getRecommendation(stockPrice, COMMISSION_FEE, CurrencyConversionRateProducer(),
                            stockOrderRepository.list(stockPrice.name))
            _recommendationViewState.postValue(when (recommendation) {
                is Recommendation -> RecommendationViewState.OK(recommendation)
                else -> RecommendationViewState.NONE
            })
            val repository = StockOrderRepositoryImpl(context)
            val latestScore = CromFortuneV1AlgorithmConformanceScoreCalculator().getScore(
                    CromFortuneV1RecommendationAlgorithm(context, repository), stocks(repository).toSet())
            _score.postValue("Du följer Croms vilja till " + latestScore.score + "%")
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
