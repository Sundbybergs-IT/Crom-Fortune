package com.sundbybergsit.cromfortune.stocks

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sundbybergsit.cromfortune.ui.home.StockPrice
import java.time.Instant

object StockPriceRepository {

    private const val TAG = "StockPriceRepository"

    @Suppress("ObjectPropertyName")
    private val _stockPrices = MutableLiveData<ViewState>()

    val stockPrices: LiveData<ViewState> = _stockPrices

    fun put(stockPrice: Set<StockPrice>) {
        Log.v(TAG, "put(${stockPrice})")
        _stockPrices.postValue(ViewState.VALUES(Instant.now(), stockPrice))
    }

    sealed class ViewState {
        data class VALUES(val instant: Instant, val stockPrices: Set<StockPrice>) : ViewState()
    }

}
