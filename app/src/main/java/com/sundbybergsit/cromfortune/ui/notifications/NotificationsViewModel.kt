package com.sundbybergsit.cromfortune.ui.notifications

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.ui.dashboard.NotificationsRepositoryImpl
import com.sundbybergsit.cromfortune.ui.home.*
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {

    private val _score = MutableLiveData<String>().apply {
        value = ""
    }
    val score: LiveData<String> = _score

    private val _notifications = MutableLiveData<ViewState>()
    val notifications: LiveData<ViewState> = _notifications

    fun refresh(context: Context) {
        viewModelScope.launch {
            val repository = StockOrderRepositoryImpl(context)
            val latestScore = CromFortuneV1AlgorithmConformanceScoreCalculator().getScore(
                    CromFortuneV1RecommendationAlgorithm(context, repository), stocks(repository).toSet())
            _score.postValue("Du följer Croms vilja till " + latestScore.score + "%")
            val notifications = NotificationsRepositoryImpl(context).list()
            if (notifications.isEmpty()) {
                _notifications.postValue(ViewState.HasNoNotifications(R.string.generic_error_empty))
            } else {
                _notifications.postValue(ViewState.HasNotifications(R.string.notifications_title,
                        NotificationAdapterItemUtil.convertToAdapterItems(notifications)))
            }
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

    sealed class ViewState {

        data class HasNotifications(@StringRes val textResId: Int, val adapterItems: List<AdapterItem>) : ViewState()

        data class HasNoNotifications(@StringRes val textResId: Int) : ViewState()

    }

}
