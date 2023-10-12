package com.sundbybergsit.cromfortune.main.ui.home

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.foundation.pager.PagerState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sundbybergsit.cromfortune.algorithm.api.RecommendationAlgorithm
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockEventApi
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockOrderAggregate
import com.sundbybergsit.cromfortune.domain.StockOrderApi
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.StockSplit
import com.sundbybergsit.cromfortune.main.CromFortuneApp
import com.sundbybergsit.cromfortune.main.Databases
import com.sundbybergsit.cromfortune.main.StockDataRetrievalCoroutineWorker
import com.sundbybergsit.cromfortune.main.crom.CromFortuneV1RecommendationAlgorithm
import com.sundbybergsit.cromfortune.main.currencies.CurrencyRateRepository
import com.sundbybergsit.cromfortune.main.stocks.StockEventRepository
import com.sundbybergsit.cromfortune.main.stocks.StockOrderRepository
import com.sundbybergsit.cromfortune.main.stocks.StockPriceRepository
import com.sundbybergsit.cromfortune.main.stocks.StockSplitRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Currency

class HomeViewModel(
    private val portfolioSharedPreferences: SharedPreferences,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    companion object {

        const val TAG: String = "HomeViewModel"

        const val DEFAULT_PORTFOLIO_NAME = "Default"
        const val CROM_PORTFOLIO_NAME = "Crom"

    }

    private val _portfoliosStateFlow: MutableStateFlow<MutableMap<String, ViewState>> = MutableStateFlow(mutableMapOf())
    internal val portfoliosStateFlow: StateFlow<Map<String, ViewState>> = _portfoliosStateFlow.asStateFlow()

    private var showAll = false

    val changedPagerMutableStateFlow : MutableStateFlow<Boolean> = MutableStateFlow(false)

    private val _selectedPorfolioNameStateFlow: MutableStateFlow<String> = MutableStateFlow(DEFAULT_PORTFOLIO_NAME)
    val selectedPorfolioNameStateFlow: StateFlow<String> = _selectedPorfolioNameStateFlow.asStateFlow()

    init {
        val allPortfolioNames =
            portfolioSharedPreferences.getStringSet(Databases.PORTFOLIO_DB_KEY_NAME_STRING_SET, setOf())!!
        val portfolioViewStates: MutableMap<String, ViewState> = mutableMapOf()
        for (portfolioName in allPortfolioNames.iterator()) {
            Log.d(TAG, "Adding portfolio $portfolioName")
            portfolioViewStates[portfolioName] = ViewState(listOf(), readOnly = portfolioName == CROM_PORTFOLIO_NAME)
        }
        _portfoliosStateFlow.value = portfolioViewStates
    }

    private fun getCalculatedStockOrderAggregate(sortedStockEvents: List<StockEvent>): StockOrderAggregate {
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
        return stockOrderAggregate!!
    }

    private fun getCalculatedStockOrderAggregate(
        stockEvents: List<StockEvent>,
        recommendationAlgorithm: RecommendationAlgorithm,
    ): StockOrderAggregate {
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
                        recommendationAlgorithm = recommendationAlgorithm
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
        return stockOrderAggregate!!
    }

    fun selectTab(porfolioName: String, index: Int, pagerState: PagerState, coroutineScope: CoroutineScope) {
        this._selectedPorfolioNameStateFlow.value = porfolioName
        coroutineScope.launch {
            pagerState.scrollToPage(page = index, pageOffsetFraction = 0f)
        }
    }

    private fun refresh(context: Context) {
        val allPortfolioNames =
            portfolioSharedPreferences.getStringSet(Databases.PORTFOLIO_DB_KEY_NAME_STRING_SET, setOf())!!
        val portfolioViewStates: MutableMap<String, ViewState> = mutableMapOf()
        for (portfolioName in allPortfolioNames.iterator()) {
            if (portfolioName == CROM_PORTFOLIO_NAME) {
                // This will be populated through the default portfolio which is used as reference
                continue
            }
            Log.d(TAG, "Adding $portfolioName portfolio")
            portfolioViewStates[portfolioName] = ViewState(
                items = stocks(context = context, portfolioName = portfolioName, lambda = { sortedStockEvents ->
                    getCalculatedStockOrderAggregate(sortedStockEvents)
                }), readOnly = false
            )
            if (portfolioName == DEFAULT_PORTFOLIO_NAME) {
                Log.d(TAG, "Adding Crom portfolio")
                portfolioViewStates[CROM_PORTFOLIO_NAME] = ViewState(
                    items = stocks(
                        context = context,
                        portfolioName = DEFAULT_PORTFOLIO_NAME,
                        lambda = { stockEvents ->
                            getCalculatedStockOrderAggregate(stockEvents, CromFortuneV1RecommendationAlgorithm(context))
                        }), readOnly = true
                )
            }
        }
        _portfoliosStateFlow.value = portfolioViewStates
    }

    fun save(context: Context, stockSplit: StockSplit) {
        Log.i(TAG, "save(stockSplit=[$stockSplit])")
        val stockSplitRepository =
            StockSplitRepository(context = context, porfolioName = selectedPorfolioNameStateFlow.value)
        if (stockSplitRepository.list(stockSplit.name).isNotEmpty()) {
            val existingSplits = stockSplitRepository.list(stockSplit.name)
            stockSplitRepository.putAll(stockSplit.name, existingSplits.toMutableSet() + stockSplit)
        } else {
            stockSplitRepository.putReplacingAll(stockSplit.name, stockSplit)
        }
        refresh(context)
    }

    fun save(context: Context, portfolioName: String, stockOrder: StockOrder) {
        Log.i(TAG, "save(portfolioName=[$portfolioName], stockOrder=[$stockOrder])")
        val stockOrderApi: StockOrderApi =
            StockOrderRepository(context = context, porfolioName = portfolioName)
        if (stockOrderApi.list(stockOrder.name).isNotEmpty()) {
            val existingOrders = stockOrderApi.list(stockOrder.name)
            stockOrderApi.putAll(stockOrder.name, existingOrders.toMutableSet() + stockOrder)
        } else {
            stockOrderApi.putReplacingAll(stockOrder.name, stockOrder)
        }
        refresh(context)
    }

    fun stocks(context: Context, portfolioName: String, lambda: (List<StockEvent>) -> StockOrderAggregate):
        List<StockOrderAggregate> {
        val stockEventApi: StockEventApi = StockEventRepository(context, portfolioName = portfolioName)
        val stockOrderAggregates: MutableList<StockOrderAggregate> = mutableListOf()
        for (stockSymbol in stockEventApi.listOfStockNames()) {
            val stockEvents: Set<StockEvent> = stockEventApi.list(stockSymbol)
            if (stockEvents.isEmpty()) {
                // Preventive cleanup, https://github.com/Sundbybergs-IT/Crom-Fortune/issues/20
                stockEventApi.remove(stockSymbol)
            } else {
                val sortedStockOrders: List<StockEvent> = stockEvents.sortedBy { event -> event.dateInMillis }
                val stockAggregate = lambda(sortedStockOrders)
                if (!showAll && stockAggregate.getQuantity() == 0) {
                    Log.i(TAG, "Hiding this stock because of the filter option.")
                } else {
                    stockOrderAggregates.add(stockAggregate)
                }
            }
        }
        return stockOrderAggregates.sortedBy { stockOrderAggregate -> stockOrderAggregate.displayName }
    }

    fun portfolioStockEvents(context: Context, portfolioName: String, stockSymbol: String): List<StockEvent> {
        return if (portfolioName == CROM_PORTFOLIO_NAME) {
            stocks(context = context, portfolioName = portfolioName) { sortedStockEvents ->
                getCalculatedStockOrderAggregate(sortedStockEvents, CromFortuneV1RecommendationAlgorithm(context))
            }.find { stockOrderAggregate -> stockOrderAggregate.stockSymbol == stockSymbol }!!.events.toList()
        } else {
            stocks(context = context, portfolioName = portfolioName) { sortedStockEvents ->
                getCalculatedStockOrderAggregate(sortedStockEvents)
            }.find { stockOrderAggregate -> stockOrderAggregate.stockSymbol == stockSymbol }!!.events.toList()
        }
    }

    fun hasNumberOfStocks(context: Context, portfolioName: String, stockName: String, quantity: Int): Boolean {
        return StockOrderRepository(context, porfolioName = portfolioName).count(stockName) >= quantity
    }

    fun confirmRemove(context: Context, stockName: String) {
        val stockOrderApi: StockOrderApi = StockOrderRepository(context, porfolioName = selectedPorfolioNameStateFlow.value)
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
        refresh(context)
    }

    fun showCurrent(context: Context) {
        showAll = false
        refresh(context)
    }

    fun sortNameAscending(portfolioName: String) {
        _portfoliosStateFlow.value.let { mutableMap ->
            val oldViewState = mutableMap[portfolioName]!!
            val sortedPortfolio = oldViewState.items.sortedBy { item -> item.displayName }
            mutableMap.replace(portfolioName, ViewState(sortedPortfolio, oldViewState.readOnly))
        }
    }

    fun sortNameDescending(portfolioName: String) {
        _portfoliosStateFlow.value.let { mutableMap ->
            val oldViewState = mutableMap[portfolioName]!!
            val sortedPortfolio = oldViewState.items.sortedByDescending { item -> item.displayName }
            mutableMap.replace(portfolioName, ViewState(sortedPortfolio, oldViewState.readOnly))
        }
    }

    fun sortProfitAscending(portfolioName: String) {
        _portfoliosStateFlow.value.let { mutableMap ->
            val oldViewState = mutableMap[portfolioName]!!
            val sortedPortfolio = oldViewState.items.sortedBy { item ->
                item.getProfit(
                    StockPriceRepository.getStockPrice(
                        item.stockSymbol
                    ).price
                )
            }
            mutableMap.replace(portfolioName, ViewState(sortedPortfolio, oldViewState.readOnly))
        }
    }

    fun sortProfitDescending(portfolioName: String) {
        _portfoliosStateFlow.value.let { mutableMap ->
            val oldViewState = mutableMap[portfolioName]!!
            val sortedPortfolio = oldViewState.items.sortedByDescending { item ->
                item.getProfit(
                    StockPriceRepository.getStockPrice(
                        item.stockSymbol
                    ).price
                )
            }
            mutableMap.replace(portfolioName, ViewState(sortedPortfolio, oldViewState.readOnly))
        }
    }

    fun savePortolio(portfolioName: String) {
        Log.i(TAG, "Save new portfolio: $portfolioName")
        val portfolioNames =
            portfolioSharedPreferences.getStringSet(Databases.PORTFOLIO_DB_KEY_NAME_STRING_SET, emptySet())!!
                .toMutableSet()
        portfolioNames.add(portfolioName)
        portfolioSharedPreferences.edit()
            .putStringSet(Databases.PORTFOLIO_DB_KEY_NAME_STRING_SET, portfolioNames.toSet()).apply()
    }

    internal class ViewState(val items: List<StockOrderAggregate>, val readOnly: Boolean)

}
