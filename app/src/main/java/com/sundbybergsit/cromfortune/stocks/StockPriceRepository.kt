package com.sundbybergsit.cromfortune.stocks

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.time.Instant

object StockPriceRepository {

    private const val TAG = "StockPriceRepository"

    @Suppress("ObjectPropertyName")
    private val _stockPrices = MutableLiveData<ViewState>(ViewState.NotInitialized)

    val stockPrices: LiveData<ViewState> = _stockPrices

    fun put(stockPrice: Set<com.sundbybergsit.cromfortune.domain.StockPrice>) {
        Log.v(TAG, "put(${stockPrice})")
        _stockPrices.postValue(ViewState.VALUES(Instant.now(), stockPrice))
    }

    sealed class ViewState {
        object NotInitialized : ViewState()
        data class VALUES(val instant: Instant, val stockPrices: Set<com.sundbybergsit.cromfortune.domain.StockPrice>) : ViewState()
    }

}
