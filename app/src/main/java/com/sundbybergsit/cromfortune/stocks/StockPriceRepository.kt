package com.sundbybergsit.cromfortune.stocks

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sundbybergsit.cromfortune.domain.StockPrice
import java.time.Instant

object StockPriceRepository : StockPriceListener {

    private const val TAG = "StockPriceRepository"

    @Suppress("ObjectPropertyName")
    private val _stockPrices = MutableLiveData<ViewState>(ViewState.NotInitialized)

    val stockPrices: LiveData<ViewState> = _stockPrices

    fun put(stockPrice: Set<StockPrice>) {
        Log.v(TAG, "put(${stockPrice})")
        _stockPrices.postValue(ViewState.VALUES(Instant.now(), stockPrice))
    }

    override fun getStockPrice(stockSymbol: String): StockPrice {
        return (stockPrices.value as ViewState.VALUES)
            .stockPrices.find { stockPrice -> stockPrice.stockSymbol == stockSymbol }!!
    }

    sealed class ViewState {
        object NotInitialized : ViewState()
        data class VALUES(val instant: Instant, val stockPrices: Set<StockPrice>) : ViewState()
    }

}
