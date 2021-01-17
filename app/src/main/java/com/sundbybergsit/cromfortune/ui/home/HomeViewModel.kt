package com.sundbybergsit.cromfortune.ui.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.stocks.StockOrderRepository

class HomeViewModel : ViewModel(), StockRemovable {

    private val _viewState = MutableLiveData<ViewState>()
    private val _stockTransactionState = MutableLiveData<StockTransactionState>()

    val viewState: LiveData<ViewState> = _viewState
    val stockTransactionState: LiveData<StockTransactionState> = _stockTransactionState

    @SuppressLint("ApplySharedPref")
    override fun remove(context: Context, stockName: String) {
        val stockOrderRepository: StockOrderRepository = StockOrderRepositoryImpl(context)
        stockOrderRepository.remove(stockName)
        refresh(context)
    }

    fun refresh(context: Context) {
        val stockOrderRepository: StockOrderRepository = StockOrderRepositoryImpl(context)
        if (stockOrderRepository.isEmpty()) {
            _viewState.postValue(ViewState.HasNoStocks(R.string.home_no_stocks))
        } else {
            _viewState.postValue(ViewState.HasStocks(R.string.home_stocks,
                    AdapterItemUtil.convertToAdapterItems(stocks(context))))
        }
    }

    fun save(context: Context, stockOrder: StockOrder) {
        val stockOrderRepository: StockOrderRepository = StockOrderRepositoryImpl(context)
        if (stockOrderRepository.list(stockOrder.name).isNotEmpty()) {
            val existingOrders = stockOrderRepository.list(stockOrder.name)
            stockOrderRepository.putAll(stockOrder.name, existingOrders.toMutableSet() + stockOrder)
            _stockTransactionState.postValue(StockTransactionState.Saved)
        } else {
            stockOrderRepository.put(stockOrder.name, stockOrder)
            _stockTransactionState.postValue(StockTransactionState.Saved)
        }
        refresh(context)
    }

    private fun stocks(context: Context): List<StockOrder> {
        val stockOrderRepository: StockOrderRepository = StockOrderRepositoryImpl(context)
        val aggregatedStockOrders: MutableList<StockOrder> = mutableListOf()
        for (stockName in stockOrderRepository.listOfStockNames()) {
            val stockOrders: Set<StockOrder> = stockOrderRepository.list(stockName)
            var quantity = 0
            var accumulatedCost = 0.0
            var currency: String? = null
            for (stockOrder in stockOrders) {
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

    fun hasNumberOfStocks(context: Context, stockName: String, quantity: Int): Boolean {
        return StockOrderRepositoryImpl(context).count(stockName) >= quantity
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
