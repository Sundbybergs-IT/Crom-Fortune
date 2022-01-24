package com.sundbybergsit.cromfortune.ui.home

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sundbybergsit.cromfortune.CromFortuneApp
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.StockDataRetrievalCoroutineWorker
import com.sundbybergsit.cromfortune.crom.CromFortuneV1RecommendationAlgorithm
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.domain.*
import com.sundbybergsit.cromfortune.stocks.StockEventRepositoryImpl
import com.sundbybergsit.cromfortune.stocks.StockOrderRepositoryImpl
import com.sundbybergsit.cromfortune.stocks.StockSplitRepositoryImpl
import com.sundbybergsit.cromfortune.ui.home.view.NameAndValueAdapterItem
import com.sundbybergsit.cromfortune.ui.home.view.StockRemoveClickListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class HomeViewModel : ViewModel(), StockRemoveClickListener {

    companion object {

        const val TAG: String = "HomeViewModel"

    }

    private val _cromStocksViewState = MutableLiveData<ViewState>(ViewState.Loading)
    private val _personalStocksViewState = MutableLiveData<ViewState>(ViewState.Loading)
    private val _dialogViewState = MutableLiveData<DialogViewState>()

    val cromStocksViewState: LiveData<ViewState> = _cromStocksViewState
    val personalStocksViewState: LiveData<ViewState> = _personalStocksViewState
    val dialogViewState: LiveData<DialogViewState> = _dialogViewState
    private var showAll = false

    private val cromStockAggregate: (List<StockEvent>, Context) -> StockOrderAggregate =
        { stockEvents, context ->
            val sortedStockEvents = stockEvents.sortedBy { it.dateInMillis }
            var stockOrderAggregate: StockOrderAggregate? = null
            val cromSortedStockEvents: MutableList<StockEvent> = mutableListOf()
            for (stockEvent in sortedStockEvents) {
                if (stockOrderAggregate == null && stockEvent.stockSplit != null) {
                    // Ignore splits before first stock order
                } else if (stockOrderAggregate == null) {
                    val stockOrder = checkNotNull(stockEvent.stockOrder)
                    val stockName =
                        StockPrice.SYMBOLS.find { pair -> pair.first == stockOrder.name }!!.second
                    stockOrderAggregate = StockOrderAggregate(
                        (CurrencyRateRepository.currencyRates.value as CurrencyRateRepository.ViewState.VALUES)
                            .currencyRates.find { currencyRate -> currencyRate.iso4217CurrencySymbol == stockOrder.currency }!!.rateInSek,
                        "$stockName (${stockOrder.name})", stockOrder.name,
                        Currency.getInstance(stockOrder.currency)
                    )
                    cromSortedStockEvents.add(stockEvent)
                    stockOrderAggregate.aggregate(stockEvent)
                } else if (stockEvent.stockOrder != null) {
                    val possibleNewStockEvent: StockEvent? =
                        stockOrderAggregate.applyStockOrderForRecommendationAlgorithm(
                            eventToConsider = stockEvent,
                            existingEvents = cromSortedStockEvents,
                            recommendationAlgorithm = CromFortuneV1RecommendationAlgorithm(context)
                        )
                    if (possibleNewStockEvent != null) {
                        cromSortedStockEvents.add(possibleNewStockEvent)
                        stockOrderAggregate.aggregate(possibleNewStockEvent)
                    }
                } else {
                    stockOrderAggregate.aggregate(stockEvent)
                }
            }
            stockOrderAggregate!!
        }

    private val personalStockAggregate: (List<StockEvent>, Context) -> StockOrderAggregate =
        { sortedStockEvents, _ ->
            var stockOrderAggregate: StockOrderAggregate? = null
            for (stockEvent in sortedStockEvents) {
                if (stockOrderAggregate == null && stockEvent.stockSplit != null) {
                    // Ignore splits before first stock order
                } else if (stockOrderAggregate == null) {
                    val stockOrder = checkNotNull(stockEvent.stockOrder)
                    val stockName =
                        StockPrice.SYMBOLS.find { pair -> pair.first == stockOrder.name }!!.second
                    stockOrderAggregate = StockOrderAggregate(
                        (CurrencyRateRepository.currencyRates.value as CurrencyRateRepository.ViewState.VALUES)
                            .currencyRates.find { currencyRate -> currencyRate.iso4217CurrencySymbol == stockOrder.currency }!!.rateInSek,
                        "$stockName (${stockOrder.name})", stockOrder.name,
                        Currency.getInstance(stockOrder.currency)
                    )
                    stockOrderAggregate.aggregate(stockEvent)
                } else {
                    stockOrderAggregate.aggregate(stockEvent)
                }
            }
            stockOrderAggregate!!
        }

    @SuppressLint("ApplySharedPref")
    override fun onClickRemove(context: Context, stockName: String) {
        _dialogViewState.postValue(DialogViewState.ShowDeleteDialog(stockName))
    }

    fun refresh(context: Context) {
        val stockEventRepository: StockEventRepository = StockEventRepositoryImpl(context)
        if (stockEventRepository.isEmpty()) {
            _cromStocksViewState.postValue(ViewState.HasNoStocks(R.string.home_no_stocks))
            _personalStocksViewState.postValue(ViewState.HasNoStocks(R.string.home_no_stocks))
        } else {
            viewModelScope.launch {
                _cromStocksViewState.postValue(
                    ViewState.HasStocks(
                        R.string.home_stocks,
                        StockAggregateAdapterItemUtil.convertToAdapterItems(
                            list = stocks(
                                context = context,
                                lambda = cromStockAggregate
                            )
                        )
                    )
                )
                _personalStocksViewState.postValue(
                    ViewState.HasStocks(
                        R.string.home_stocks,
                        StockAggregateAdapterItemUtil.convertToAdapterItems(
                            list = stocks(
                                context = context,
                                lambda = personalStockAggregate
                            )
                        )
                    )
                )
            }
        }
    }

    fun save(context: Context, stockSplit: StockSplit) {
        val stockSplitRepository = StockSplitRepositoryImpl(context = context)
        if (stockSplitRepository.list(stockSplit.name).isNotEmpty()) {
            val existingSplits = stockSplitRepository.list(stockSplit.name)
            stockSplitRepository.putAll(stockSplit.name, existingSplits.toMutableSet() + stockSplit)
        } else {
            stockSplitRepository.putReplacingAll(stockSplit.name, stockSplit)
        }
        refresh(context)
    }

    fun save(context: Context, stockOrder: StockOrder) {
        val stockOrderRepository: StockOrderRepository = StockOrderRepositoryImpl(context)
        if (stockOrderRepository.list(stockOrder.name).isNotEmpty()) {
            val existingOrders = stockOrderRepository.list(stockOrder.name)
            stockOrderRepository.putAll(stockOrder.name, existingOrders.toMutableSet() + stockOrder)
        } else {
            stockOrderRepository.putReplacingAll(stockOrder.name, stockOrder)
        }
        refresh(context)
    }

    fun personalStockEvents(context: Context, stockSymbol: String): List<StockEvent> {
        return stocks(context, personalStockAggregate)
            .find { stockOrderAggregate -> stockOrderAggregate.stockSymbol == stockSymbol }!!.events.toList()
    }

    fun stocks(context: Context, lambda: (List<StockEvent>, Context) -> StockOrderAggregate):
            List<StockOrderAggregate> {
        val stockEventRepository: StockEventRepository = StockEventRepositoryImpl(context)
        val stockOrderAggregates: MutableList<StockOrderAggregate> = mutableListOf()
        for (stockSymbol in stockEventRepository.listOfStockNames()) {
            val stockEvents: Set<StockEvent> = stockEventRepository.list(stockSymbol)
            if (stockEvents.isEmpty()) {
                // Preventive cleanup, https://github.com/Sundbybergs-IT/Crom-Fortune/issues/20
                stockEventRepository.remove(stockSymbol)
            } else {
                val sortedStockOrders: List<StockEvent> = stockEvents.sortedBy { event -> event.dateInMillis }
                val stockAggregate = lambda(sortedStockOrders, context)
                if (!showAll && stockAggregate.getQuantity() == 0) {
                    Log.i(TAG, "Hiding this stock because of the filter option.")
                } else {
                    stockOrderAggregates.add(stockAggregate)
                }
            }
        }
        return stockOrderAggregates.sortedBy { stockOrderAggregate -> stockOrderAggregate.displayName }
    }

    fun cromStockEvents(context: Context, stockSymbol: String): List<StockEvent> {
        return stocks(context, cromStockAggregate)
            .find { stockOrderAggregate -> stockOrderAggregate.stockSymbol == stockSymbol }!!.events.toList()
    }

    fun hasNumberOfStocks(context: Context, stockName: String, quantity: Int): Boolean {
        return StockOrderRepositoryImpl(context).count(stockName) >= quantity
    }

    fun confirmRemove(context: Context, stockName: String) {
        val stockOrderRepository: StockOrderRepository = StockOrderRepositoryImpl(context)
        stockOrderRepository.remove(stockName)
        refresh(context)
    }

    fun refreshData(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            StockDataRetrievalCoroutineWorker.refreshFromYahoo(context)
            Log.i(TAG, "Last refreshed: " + (context.applicationContext as CromFortuneApp).lastRefreshed)
        }
    }

    fun showAll(context: Context) {
        showAll = true
        if (_personalStocksViewState.value is ViewState.HasStocks) {
            refresh(context)
        }
    }

    fun showCurrent(context: Context) {
        showAll = false
        if (_personalStocksViewState.value is ViewState.HasStocks) {
            refresh(context)
        }
    }

    sealed class DialogViewState {

        data class ShowDeleteDialog(val stockName: String) : DialogViewState()

    }

    sealed class ViewState {

        object Loading : ViewState()

        data class HasStocks(@StringRes val textResId: Int, val adapterItems: List<NameAndValueAdapterItem>) :
            ViewState()

        data class HasNoStocks(@StringRes val textResId: Int) : ViewState()

    }

}
