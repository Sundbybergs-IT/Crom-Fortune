package com.sundbybergsit.cromfortune.main.ui.home

import android.content.Context
import android.util.Log
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockEventApi
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderAggregate
import com.sundbybergsit.cromfortune.domain.StockOrderApi
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.StockSplit
import com.sundbybergsit.cromfortune.main.CromFortuneApp
import com.sundbybergsit.cromfortune.main.StockDataRetrievalCoroutineWorker
import com.sundbybergsit.cromfortune.main.crom.CromFortuneV1RecommendationAlgorithm
import com.sundbybergsit.cromfortune.main.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.main.stocks.StockEventRepository
import com.sundbybergsit.cromfortune.main.stocks.StockOrderRepository
import com.sundbybergsit.cromfortune.main.stocks.StockPriceRepository
import com.sundbybergsit.cromfortune.main.stocks.StockSplitRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Currency

class HomeViewModel(private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO) : ViewModel() {

    companion object {

        const val TAG: String = "HomeViewModel"

    }

    private val _cromStocksViewState: MutableStateFlow<ViewState> =
        MutableStateFlow(ViewState(listOf()))
    private val _personalStocksViewState: MutableStateFlow<ViewState> =
        MutableStateFlow(ViewState(listOf()))

    internal val cromStocksViewState: StateFlow<ViewState> = _cromStocksViewState.asStateFlow()
    internal val personalStocksViewState: StateFlow<ViewState> = _personalStocksViewState.asStateFlow()

    private var showAll = false

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
                        rateInSek = checkNotNull(CurrencyRateRepository.currencyRates.value)
                            .find { currencyRate -> currencyRate.iso4217CurrencySymbol == stockOrder.currency }!!.rateInSek,
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
                        CurrencyRateRepository.currencyRates.value
                            .find { currencyRate -> currencyRate.iso4217CurrencySymbol == stockOrder.currency }!!.rateInSek,
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
        val stockEventApi: StockEventApi = StockEventRepository(context)
        if (stockEventApi.isEmpty()) {
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
        val stockSplitRepository = StockSplitRepository(context = context)
        if (stockSplitRepository.list(stockSplit.name).isNotEmpty()) {
            val existingSplits = stockSplitRepository.list(stockSplit.name)
            stockSplitRepository.putAll(stockSplit.name, existingSplits.toMutableSet() + stockSplit)
        } else {
            stockSplitRepository.putReplacingAll(stockSplit.name, stockSplit)
        }
        refresh(context)
    }

    fun save(context: Context, stockOrder: StockOrder) {
        val stockOrderApi: StockOrderApi = StockOrderRepository(context)
        if (stockOrderApi.list(stockOrder.name).isNotEmpty()) {
            val existingOrders = stockOrderApi.list(stockOrder.name)
            stockOrderApi.putAll(stockOrder.name, existingOrders.toMutableSet() + stockOrder)
        } else {
            stockOrderApi.putReplacingAll(stockOrder.name, stockOrder)
        }
        refresh(context)
    }

    fun personalStockEvents(context: Context, stockSymbol: String): List<StockEvent> {
        return stocks(context, personalStockAggregate)
            .find { stockOrderAggregate -> stockOrderAggregate.stockSymbol == stockSymbol }!!.events.toList()
    }

    fun stocks(context: Context, lambda: (List<StockEvent>, Context) -> StockOrderAggregate):
        List<StockOrderAggregate> {
        val stockEventApi: StockEventApi = StockEventRepository(context)
        val stockOrderAggregates: MutableList<StockOrderAggregate> = mutableListOf()
        for (stockSymbol in stockEventApi.listOfStockNames()) {
            val stockEvents: Set<StockEvent> = stockEventApi.list(stockSymbol)
            if (stockEvents.isEmpty()) {
                // Preventive cleanup, https://github.com/Sundbybergs-IT/Crom-Fortune/issues/20
                stockEventApi.remove(stockSymbol)
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
        return StockOrderRepository(context).count(stockName) >= quantity
    }

    fun confirmRemove(context: Context, stockName: String) {
        val stockOrderApi: StockOrderApi = StockOrderRepository(context)
        stockOrderApi.remove(stockName)
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

    fun sortNameAscending(profile: String) {
        when (profile) {
            "personal" -> _personalStocksViewState.value =
                ViewState(personalStocksViewState.value.items.sortedBy { item -> item.displayName })

            "crom" -> _cromStocksViewState.value =
                ViewState(cromStocksViewState.value.items.sortedBy { item -> item.displayName })

            else -> TODO("Not yet implemented")
        }
    }

    fun sortNameDescending(profile: String) {
        when (profile) {
            "personal" -> _personalStocksViewState.value =
                ViewState(personalStocksViewState.value.items.sortedByDescending { item -> item.displayName })

            "crom" -> _cromStocksViewState.value =
                ViewState(cromStocksViewState.value.items.sortedByDescending { item -> item.displayName })

            else -> TODO("Not yet implemented")
        }
    }

    fun sortProfitAscending(profile: String) {
        when (profile) {
            "personal" -> _personalStocksViewState.value =
                ViewState(personalStocksViewState.value.items.sortedBy { item ->
                    item.getProfit(
                        StockPriceRepository.getStockPrice(
                            item.stockSymbol
                        ).price
                    )
                })

            "crom" -> _cromStocksViewState.value =
                ViewState(cromStocksViewState.value.items.sortedBy { item ->
                    item.getProfit(
                        StockPriceRepository.getStockPrice(
                            item.stockSymbol
                        ).price
                    )
                })

            else -> TODO("Not yet implemented")
        }
    }

    fun sortProfitDescending(profile: String) {
        when (profile) {
            "personal" -> _personalStocksViewState.value =
                ViewState(personalStocksViewState.value.items.sortedByDescending { item ->
                    item.getProfit(
                        StockPriceRepository.getStockPrice(
                            item.stockSymbol
                        ).price
                    )
                })

            "crom" -> _cromStocksViewState.value =
                ViewState(cromStocksViewState.value.items.sortedByDescending { item ->
                    item.getProfit(
                        StockPriceRepository.getStockPrice(
                            item.stockSymbol
                        ).price
                    )
                })

            else -> TODO("Not yet implemented")
        }
    }

    internal class ViewState(val items: List<StockOrderAggregate>)

}
