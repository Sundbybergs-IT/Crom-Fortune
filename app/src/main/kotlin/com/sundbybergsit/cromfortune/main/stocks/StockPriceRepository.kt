package com.sundbybergsit.cromfortune.main.stocks

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.StockPriceApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

object StockPriceRepository : StockPriceApi {

    private const val TAG = "StockPriceRepository"

    private val _stockPricesStateFlow: MutableStateFlow<ViewState> = MutableStateFlow(getPristineViewState())

    val stockPricesStateFlow: StateFlow<ViewState> = _stockPricesStateFlow.asStateFlow()

    override fun put(stockPrice: Set<StockPrice>) {
        Log.v(TAG, "put(${stockPrice})")
        _stockPricesStateFlow.value = ViewState(Instant.now(), stockPrice)
    }

    override fun getStockPrice(stockSymbol: String): StockPrice? =
        stockPricesStateFlow.value.stockPrices.find { stockPrice -> stockPrice.stockSymbol == stockSymbol }

    @VisibleForTesting
    fun clear() {
        _stockPricesStateFlow.value = getPristineViewState()
    }

    private fun getPristineViewState() = ViewState(Instant.now(), setOf())

    class ViewState(val instant: Instant, val stockPrices: Set<StockPrice>)

}
