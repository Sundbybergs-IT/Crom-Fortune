package com.sundbybergsit.cromfortune.stocks

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.sundbybergsit.cromfortune.domain.StockPrice
import java.time.Instant

object StockPriceRepository : StockPriceListener {

    private const val TAG = "StockPriceRepository"

    private val _stockPrices: MutableState<ViewState?> = mutableStateOf(null)

    val stockPrices: State<ViewState?> = _stockPrices

    fun put(stockPrice: Set<StockPrice>) {
        Log.v(TAG, "put(${stockPrice})")
        _stockPrices.value = ViewState(Instant.now(), stockPrice)
    }

    @VisibleForTesting
    fun clear() {
        _stockPrices.value = null
    }

    override fun getStockPrice(stockSymbol: String): StockPrice {
        return checkNotNull(checkNotNull(stockPrices.value).stockPrices.find { stockPrice -> stockPrice.stockSymbol == stockSymbol })
    }

    class ViewState(val instant: Instant, val stockPrices: Set<StockPrice>)

}
