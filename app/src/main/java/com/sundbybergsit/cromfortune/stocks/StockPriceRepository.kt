package com.sundbybergsit.cromfortune.stocks

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sundbybergsit.cromfortune.ui.home.StockPrice

object StockPriceRepository {

    private const val TAG = "StockPriceRepository"

    @Suppress("ObjectPropertyName")
    private val _stockPrices = MutableLiveData<StockPrice>()

    val stockPrices: LiveData<StockPrice> = _stockPrices

    fun put(stockPrice: StockPrice) {
        Log.v(TAG, "put(${stockPrice})")
        _stockPrices.postValue(stockPrice)
    }

}
