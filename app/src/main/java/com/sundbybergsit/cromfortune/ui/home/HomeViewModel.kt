package com.sundbybergsit.cromfortune.ui.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.stocks.StocksPreferences
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class HomeViewModel : ViewModel(), StockRemovable {

    private val _viewState = MutableLiveData<ViewState>()
    private val _stockTransactionState = MutableLiveData<StockTransactionState>()

    val viewState: LiveData<ViewState> = _viewState
    val stockTransactionState: LiveData<StockTransactionState> = _stockTransactionState

    @SuppressLint("ApplySharedPref")
    override fun remove(context: Context, stockName: String) {
        val sharedPreferences = context.getSharedPreferences(StocksPreferences.PREFERENCES_NAME, Context.MODE_PRIVATE)
        sharedPreferences.edit().remove(stockName).commit()
        refresh(context)
    }

    fun refresh(context: Context) {
        val sharedPreferences = context.getSharedPreferences(StocksPreferences.PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (sharedPreferences.all.isEmpty()) {
            _viewState.postValue(ViewState.HasNoStocks(R.string.home_no_stocks))
        } else {
            _viewState.postValue(ViewState.HasStocks(R.string.home_stocks,
                    AdapterItemUtil.convertToAdapterItems(stocks(context))))
        }
    }

    fun save(context: Context, stockOrder: StockOrder) {
        val sharedPreferences = context.getSharedPreferences(StocksPreferences.PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (sharedPreferences.contains(stockOrder.name)) {
            val existingOrders = sharedPreferences.getStringSet(stockOrder.name, emptySet())!!
            sharedPreferences.edit().putStringSet(stockOrder.name, (existingOrders.toMutableSet() +
                    mutableSetOf(Json.encodeToString(stockOrder))).toMutableSet()).commit()
            _stockTransactionState.postValue(StockTransactionState.Saved)
        } else {
            sharedPreferences.edit()
                    .putStringSet(stockOrder.name, setOf(Json.encodeToString(stockOrder))).apply()
            _stockTransactionState.postValue(StockTransactionState.Saved)
        }
        refresh(context)
    }

    private fun stocks(context: Context): List<StockOrder> {
        val sharedPreferences = context.getSharedPreferences(StocksPreferences.PREFERENCES_NAME, Context.MODE_PRIVATE)

        val aggregatedStockOrders: MutableList<StockOrder> = mutableListOf()
        for (stockName in sharedPreferences.all.keys) {
            val stockOrders: Set<String> = sharedPreferences.all[stockName] as Set<String>
            var quantity = 0
            var accumulatedCost = 0.0
            var currency: String? = null
            for (stockOrderString in stockOrders) {
                val stockOrder: StockOrder = Json.decodeFromString(stockOrderString)
                if (currency == null) {
                    currency = stockOrder.currency
                }
                when (stockOrder.orderAction) {
                    "Buy" -> {
                        quantity += stockOrder.quantity
                        accumulatedCost += (stockOrder.pricePerStock * stockOrder.quantity + stockOrder.commissionFee)
                    }
                    "Sell" -> {
                        quantity -= stockOrder.quantity
                        accumulatedCost += (-stockOrder.pricePerStock * stockOrder.quantity + stockOrder.commissionFee)
                    }
                    else -> {
                        throw IllegalStateException("Invalid stock order action: ${stockOrder.orderAction}")
                    }
                }
            }
            aggregatedStockOrders.add(StockOrder("Buy", currency!!, System.currentTimeMillis(), stockName,
                    accumulatedCost / quantity, 0.0, quantity))
        }
        return aggregatedStockOrders
    }

    sealed class ViewState {

        data class HasStocks(@StringRes val textResId: Int, val adapterItems: List<AdapterItem>) : ViewState()

        data class HasNoStocks(@StringRes val textResId: Int) : ViewState()

    }

    sealed class StockTransactionState {

        object Saved : StockTransactionState()

        data class Error(@StringRes val errorResId: Int) : StockTransactionState()

    }

}
