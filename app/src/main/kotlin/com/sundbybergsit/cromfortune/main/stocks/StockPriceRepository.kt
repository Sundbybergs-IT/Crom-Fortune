package com.sundbybergsit.cromfortune.main.stocks

import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.StockPriceApi
import java.time.Instant

object StockPriceRepository : StockPriceApi {

    private const val TAG = "StockPriceRepository"

    private val _stockPrices: MutableState<ViewState?> = mutableStateOf(null)

    val stockPrices: State<ViewState?> = _stockPrices

    override fun put(stockPrice: Set<StockPrice>) {
        Log.v(TAG, "put(${stockPrice})")
        _stockPrices.value = ViewState(Instant.now(), stockPrice)
    }

    override fun getStockPrice(stockSymbol: String): StockPrice {
        return checkNotNull(checkNotNull(stockPrices.value).stockPrices.find { stockPrice -> stockPrice.stockSymbol == stockSymbol })
    }

    @VisibleForTesting
    fun clear() {
        _stockPrices.value = null
    }

    class ViewState(val instant: Instant, val stockPrices: Set<StockPrice>)

}
