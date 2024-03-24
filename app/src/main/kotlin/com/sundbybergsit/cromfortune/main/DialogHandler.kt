package com.sundbybergsit.cromfortune.main

import android.content.Context
import android.util.Log
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockEventApi
import com.sundbybergsit.cromfortune.domain.StockPrice.Companion.SYMBOLS
import com.sundbybergsit.cromfortune.main.settings.StockRetrievalSettings
import com.sundbybergsit.cromfortune.main.stocks.StockEventRepository
import com.sundbybergsit.cromfortune.main.ui.home.view.StockRemoveClickListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DialogHandler {

    private const val TAG = "DialogHandler"

    private val _snackbarFlow: MutableStateFlow<Pair<String, Pair<String, () -> Unit>?>?> = MutableStateFlow(null)
    val snackbarFlow = _snackbarFlow as StateFlow<Pair<String, Pair<String, () -> Unit>?>?>

    private val _dialogViewState: MutableStateFlow<DialogViewState> = MutableStateFlow(DialogViewState.Dismissed)

    val dialogViewState: StateFlow<DialogViewState> = _dialogViewState.asStateFlow()

    fun showSnack(text: String, action: Pair<String,() -> Unit>? = null) {
        if (action == null) {
            Log.i(TAG, "showSnack(text=[${text}])")
        } else {
            Log.i(TAG, "showSnack(text=[${text}], action=[${action.first}, ${action.second}])")
        }
        _snackbarFlow.value = Pair(text, action)
    }

    @JvmStatic
    fun acknowledgeSnack() {
        Log.i(TAG, "acknowledgeSnack()")
        _snackbarFlow.value = null
    }

    fun showDeleteDialog(context: Context, portfolioName: String, stockName: String) {
        _dialogViewState.value = DialogViewState.ShowDeleteDialog(
            stockEventApi = StockEventRepository(context = context, portfolioName = portfolioName),
            stockName = stockName
        )
    }

    fun dismissDialog() {
        _dialogViewState.value = DialogViewState.Dismissed
    }

    fun showStockRetrievalTimeIntervalsDialog(stockRetrievalSettings: StockRetrievalSettings) {
        _dialogViewState.value =
            DialogViewState.ShowStockRetrievalTimeIntervalsDialog(stockRetrievalSettings = stockRetrievalSettings)
    }

    fun showSupportedStocksDialog() {
        val allStocks = SYMBOLS
        var message = ""
        for (stock in allStocks) {
            message += "$stock, "
        }
        _dialogViewState.value = DialogViewState.ShowSupportedStocksDialog(text = message)
    }

    fun showBuyStockDialog(stockSymbol: String? = null) {
        _dialogViewState.value = DialogViewState.ShowBuyStockDialog(stockSymbol = stockSymbol)
    }

    fun showStockEvents(stockSymbol: String, stockEvents: List<StockEvent>, readOnly: Boolean) {
        _dialogViewState.value = DialogViewState.ShowStockEvents(
            title = "${
                SYMBOLS.single { triple ->
                    triple.first == stockSymbol
                }.second
            } ($stockSymbol)", stockEvents = stockEvents, readOnly = readOnly
        )
    }

    fun showSellStockDialog(stockSymbol: String? = null) {
        _dialogViewState.value = DialogViewState.ShowSellStockDialog(stockSymbol = stockSymbol)
    }

    fun showSplitStockDialog(stockSymbol: String? = null) {
        _dialogViewState.value = DialogViewState.ShowRegisterSplitStockDialog(stockSymbol = stockSymbol)
    }

    fun showAddPortfolioDialog() {
        _dialogViewState.value = DialogViewState.ShowAddPortfolio
    }

    sealed class DialogViewState {

        data object Dismissed : DialogViewState()

        data class ShowDeleteDialog(
            val stockEventApi: StockEventApi,
            val stockName: String
        ) : DialogViewState(), StockRemoveClickListener {

            override fun onClickRemove(context: Context, stockSymbol: String) {
                stockEventApi.remove(stockSymbol)
            }

        }

        class ShowStockRetrievalTimeIntervalsDialog(val stockRetrievalSettings: StockRetrievalSettings) :
            DialogViewState() {

        }

        data class ShowSupportedStocksDialog(val text: String) : DialogViewState()
        data class ShowStockEvents(val title: String, val stockEvents: List<StockEvent>, val readOnly: Boolean) :
            DialogViewState()

        data class ShowBuyStockDialog(val stockSymbol: String? = null) : DialogViewState()

        data class ShowSellStockDialog(val stockSymbol: String? = null) : DialogViewState()

        data class ShowRegisterSplitStockDialog(val stockSymbol: String? = null) : DialogViewState()

        data object ShowAddPortfolio : DialogViewState()

    }

}
