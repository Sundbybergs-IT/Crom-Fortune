package com.sundbybergsit.cromfortune.ui.home

import android.content.Context
import android.util.Log
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sundbybergsit.cromfortune.CromFortuneApp
import com.sundbybergsit.cromfortune.StockDataRetrievalCoroutineWorker
import com.sundbybergsit.cromfortune.crom.CromFortuneV1RecommendationAlgorithm
import com.sundbybergsit.cromfortune.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockEventRepository
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderAggregate
import com.sundbybergsit.cromfortune.domain.StockOrderRepository
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.StockSplit
import com.sundbybergsit.cromfortune.stocks.StockEventRepositoryImpl
import com.sundbybergsit.cromfortune.stocks.StockOrderRepositoryImpl
import com.sundbybergsit.cromfortune.stocks.StockSplitRepositoryImpl
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Currency

class HomeViewModel(private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {

    companion object {

        const val TAG: String = "HomeViewModel"

    }

    private val _cromStocksViewState: MutableState<ViewState> =
        mutableStateOf(ViewState(listOf()))
    private val _personalStocksViewState: MutableState<ViewState> =
        mutableStateOf(ViewState(listOf()))

    internal val cromStocksViewState: State<ViewState> = _cromStocksViewState
    internal val personalStocksViewState: State<ViewState> = _personalStocksViewState

    private var showAll = false

    val showRegisterBuyStocksDialog: MutableState<Boolean> = mutableStateOf(false)
    val showRegisterSellStocksDialog: MutableState<Boolean> = mutableStateOf(false)
    val showRegisterSplitStocksDialog: MutableState<Boolean> = mutableStateOf(false)

    val changedPagerMutableState = mutableStateOf(false)

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
                        checkNotNull(StockPrice.SYMBOLS.find { pair -> pair.first == stockOrder.name }).second
                    stockOrderAggregate = StockOrderAggregate(
                        rateInSek = checkNotNull(CurrencyRateRepository.currencyRates.value).currencyRates.find { currencyRate -> currencyRate.iso4217CurrencySymbol == stockOrder.currency }!!.rateInSek,
                        displayName = "$stockName (${stockOrder.name})", stockSymbol = stockOrder.name,
                        currency = Currency.getInstance(stockOrder.currency)
                    )
                    cromSortedStockEvents.add(stockEvent)
                    stockOrderAggregate.aggregate(stockEvent)
                } else if (stockEvent.stockOrder != null) {
                    val possibleNewStockEvent: StockEvent? =
                        stockOrderAggregate.applyStockOrderForRecommendedEvent(
                            eventToConsider = stockEvent,
                            existingEvents = cromSortedStockEvents,
                            recommendationAlgorithm = CromFortuneV1RecommendationAlgorithm(context)
                        )
                    if (possibleNewStockEvent != null) {
                        cromSortedStockEvents.add(possibleNewStockEvent)
                        stockOrderAggregate.aggregate(possibleNewStockEvent)
                    }
                } else {
                    cromSortedStockEvents.add(stockEvent)
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
                        CurrencyRateRepository.currencyRates.value?.currencyRates
                            ?.find { currencyRate -> currencyRate.iso4217CurrencySymbol == stockOrder.currency }!!.rateInSek,
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

    fun selectTab(index: Int, pagerState: PagerState, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            pagerState.scrollToPage(page = index, pageOffsetFraction = 0f)
        }
    }

    private fun refresh(context: Context) {
        val stockEventRepository: StockEventRepository = StockEventRepositoryImpl(context)
        if (stockEventRepository.isEmpty()) {
            _cromStocksViewState.value = ViewState(items = listOf())
            _personalStocksViewState.value = ViewState(items = listOf())
        } else {
            _cromStocksViewState.value =
                ViewState(
                    items = stocks(context = context, lambda = cromStockAggregate)
                )
            _personalStocksViewState.value =
                ViewState(
                    items = stocks(context = context, lambda = personalStockAggregate)
                )
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

    fun refreshData(context: Context, onFinished: () -> Unit = {}) {
        viewModelScope.launch(ioDispatcher) {
            StockDataRetrievalCoroutineWorker.refreshFromYahoo(context, onFinished = {
                refresh(context)
                onFinished.invoke()
            })
            Log.i(TAG, "Last refreshed: " + (context.applicationContext as CromFortuneApp).lastRefreshed)
        }
    }

    fun showAll(context: Context) {
        showAll = true
        if (_personalStocksViewState.value.items.isNotEmpty()) {
            refresh(context)
        }
    }

    fun showCurrent(context: Context) {
        showAll = false
        if (_personalStocksViewState.value.items.isNotEmpty()) {
            refresh(context)
        }
    }

    internal class ViewState(val items: List<StockOrderAggregate>)

}
