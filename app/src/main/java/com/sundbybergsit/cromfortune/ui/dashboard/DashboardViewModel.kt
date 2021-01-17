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

    private val _viewState = MutableLiveData<RecommendationViewState>().apply {
        value = RecommendationViewState.NONE
    }

    val recommendationViewState: LiveData<RecommendationViewState> = _viewState

    fun refresh(context: Context, stockPrice: StockPrice) {

        viewModelScope.launch {
            val recommendation = CromFortuneV1Decision(context, StockOrderRepositoryImpl(context))
                    .getRecommendation(stockPrice, COMMISSION_FEE, CurrencyConversionRateProducer())
            _viewState.postValue(when (recommendation) {
                is Recommendation -> RecommendationViewState.OK(recommendation)
                else -> RecommendationViewState.NONE
            })
        }
    }

    fun stocks(context: Context): List<StockOrder> {
        val stocks = mutableListOf<StockOrder>()
        val repository = StockOrderRepositoryImpl(context)
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
