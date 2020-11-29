package com.sundbybergsit.cromfortune.ui.dashboard

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sundbybergsit.cromfortune.stocks.StocksPreferences
import com.sundbybergsit.cromfortune.ui.home.*
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

private const val COMMISSION_FEE = 39.0

class DashboardViewModel : ViewModel() {

    private val _viewState = MutableLiveData<RecommendationViewState>().apply {
        value = RecommendationViewState.NONE
    }

    val recommendationViewState: LiveData<RecommendationViewState> = _viewState

    fun refresh(context: Context, stockPrice: StockPrice) {

        viewModelScope.launch {
            val recommendation = CromFortuneV1Decision(context,
                    context.getSharedPreferences(StocksPreferences.PREFERENCES_NAME, Context.MODE_PRIVATE))
                    .getRecommendation(stockPrice, COMMISSION_FEE, CurrencyConversionRateProducer())
            _viewState.postValue(when(recommendation) {
                is Recommendation -> RecommendationViewState.OK(recommendation)
                else -> RecommendationViewState.NONE
            })
        }
    }

    fun stocks(context: Context): List<StockOrder> {
        val stocks = mutableListOf<StockOrder>()
        val sharedPreferences = context.getSharedPreferences(StocksPreferences.PREFERENCES_NAME, Context.MODE_PRIVATE)
        for (entry in sharedPreferences.all) {
            val stock : Set<String> = entry.value as Set<String>
            val iterator = stock.iterator()
            while (iterator.hasNext()) {
                stocks.add(Json.decodeFromString(iterator.next()))
            }
        }
        return stocks
    }

    sealed class RecommendationViewState {
        object NONE : RecommendationViewState()
        data class OK(val recommendation : Recommendation) : RecommendationViewState()
    }

}
