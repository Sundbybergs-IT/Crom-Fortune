package com.sundbybergsit.cromfortune.stocks

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sundbybergsit.cromfortune.ui.home.StockPrice

object StockPriceRepository {

    private const val TAG = "StockPriceRepository"

    @Suppress("ObjectPropertyName")
    private val _stockPrices = MutableLiveData<Set<StockPrice>>()

    val stockPrices: LiveData<Set<StockPrice>> = _stockPrices

    fun put(stockPrice: Set<StockPrice>) {
        Log.v(TAG, "put(${stockPrice})")
        _stockPrices.postValue(stockPrice)
    }

}
