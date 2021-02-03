package com.sundbybergsit.cromfortune.ui.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.stocks.StockOrderRepository
import com.sundbybergsit.cromfortune.stocks.StockOrderRepositoryImpl

class HomeViewModel : ViewModel(), StockRemoveClickListener {

    private val _viewState = MutableLiveData<ViewState>()
    private val _dialogViewState = MutableLiveData<DialogViewState>()
    private val _stockTransactionState = MutableLiveData<StockTransactionState>()

    val viewState: LiveData<ViewState> = _viewState
    val dialogViewState: LiveData<DialogViewState> = _dialogViewState
    val stockTransactionState: LiveData<StockTransactionState> = _stockTransactionState

    @SuppressLint("ApplySharedPref")
    override fun onClickRemove(context: Context, stockName: String) {
        _dialogViewState.postValue(DialogViewState.ShowDeleteDialog(stockName))
    }

    fun refresh(context: Context) {
        val stockOrderRepository: StockOrderRepository = StockOrderRepositoryImpl(context)
        if (stockOrderRepository.isEmpty()) {
            _viewState.postValue(ViewState.HasNoStocks(R.string.home_no_stocks))
        } else {
            _viewState.postValue(ViewState.HasStocks(R.string.home_stocks,
                    StockAdapterItemUtil.convertToAdapterItems(stocks(context))))
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
            var grossQuantity = 0
            var soldQuantity = 0
            var accumulatedCost = 0.0
            var currency: String? = null
            for (stockOrder in stockOrders) {
                if (currency == null) {
                    currency = stockOrder.currency
                }
                when (stockOrder.orderAction) {
                    "Buy" -> {
                        grossQuantity += stockOrder.quantity
                        accumulatedCost += (stockOrder.pricePerStock * stockOrder.quantity + stockOrder.commissionFee)
                    }
                    "Sell" -> {
                        soldQuantity += stockOrder.quantity
                        accumulatedCost += stockOrder.commissionFee
                    }
                    else -> {
                        throw IllegalStateException("Invalid stock order action: ${stockOrder.orderAction}")
                    }
                }
            }
            val netQuantity = grossQuantity - soldQuantity
            val averageCost = accumulatedCost / grossQuantity
            val costToExclude = averageCost * soldQuantity
            aggregatedStockOrders.add(StockOrder("Buy", currency!!, System.currentTimeMillis(), stockName,
                    (accumulatedCost - costToExclude) / netQuantity, 0.0, netQuantity))
        }
        return aggregatedStockOrders.sortedBy { stockOrder -> stockOrder.name }
    }

    fun hasNumberOfStocks(context: Context, stockName: String, quantity: Int): Boolean {
        return StockOrderRepositoryImpl(context).count(stockName) >= quantity
    }

    fun confirmRemove(context: Context, stockName: String) {
        val stockOrderRepository: StockOrderRepository = StockOrderRepositoryImpl(context)
        stockOrderRepository.remove(stockName)
        refresh(context)
    }

    sealed class DialogViewState {

        data class ShowDeleteDialog(val stockName: String) : DialogViewState()

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
