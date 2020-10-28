package com.sundbybergsit.cromfortune.ui.dashboard

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sundbybergsit.cromfortune.stocks.StocksPreferences
import com.sundbybergsit.cromfortune.ui.home.StockOrder
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class DashboardViewModel : ViewModel() {

    private val _viewState = MutableLiveData<ViewState>().apply {
        value = ViewState.OK
    }

    val viewState: LiveData<ViewState> = _viewState

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

    sealed class ViewState {
        object OK : ViewState()
    }

}
