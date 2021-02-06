package com.sundbybergsit.cromfortune.ui.home

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.stocks.StockOrderRepository
import com.sundbybergsit.cromfortune.stocks.StockOrderRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class HomeViewModel : ViewModel(), StockRemoveClickListener {

    private val _viewState = MutableLiveData<ViewState>(ViewState.Loading)
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
            viewModelScope.launch {
                _viewState.postValue(ViewState.HasStocks(R.string.home_stocks,
                        StockAggregateAdapterItemUtil.convertToAdapterItems(stocks(context))))
            }
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

    private suspend fun stocks(context: Context):
            List<StockOrderAggregate> {
        return withContext(Dispatchers.IO) {
            val stockOrderRepository: StockOrderRepository = StockOrderRepositoryImpl(context)
            val stockOrderAggregates: MutableList<StockOrderAggregate> = mutableListOf()
            for (stockName in stockOrderRepository.listOfStockNames()) {
                val stockOrders: Set<StockOrder> = stockOrderRepository.list(stockName)
                var stockOrderAggregate: StockOrderAggregate? = null
                for (stockOrder in stockOrders) {
                    if (stockOrderAggregate == null) {
                        stockOrderAggregate = StockOrderAggregate(
                                (CurrencyRateRepository.currencyRates.value as CurrencyRateRepository.ViewState.VALUES)
                                        .currencyRates.find { currencyRate -> currencyRate.iso4217CurrencySymbol == stockOrder.currency }!!.rateInSek,
                                stockOrder.name, stockOrder.name,
                                Currency.getInstance(stockOrder.currency))
                        stockOrderAggregate.aggregate(stockOrder)
                    } else {
                        stockOrderAggregate.aggregate(stockOrder)
                    }
                }
                stockOrderAggregates.add(stockOrderAggregate!!)
            }
            stockOrderAggregates.sortedBy { stockOrderAggregate -> stockOrderAggregate.displayName }
        }
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

        object Loading : ViewState()

        data class HasStocks(@StringRes val textResId: Int, val adapterItems: List<AdapterItem>) : ViewState()

        data class HasNoStocks(@StringRes val textResId: Int) : ViewState()

    }

    sealed class StockTransactionState {

        object Saved : StockTransactionState()

        data class Error(@StringRes val errorResId: Int) : StockTransactionState()

    }

}
