package com.sundbybergsit.cromfortune.main

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockEventRepository
import com.sundbybergsit.cromfortune.domain.StockPrice.Companion.SYMBOLS
import com.sundbybergsit.cromfortune.main.settings.StockRetrievalSettings
import com.sundbybergsit.cromfortune.main.stocks.StockEventRepositoryImpl
import com.sundbybergsit.cromfortune.main.ui.home.view.StockRemoveClickListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object DialogHandler {

    private const val TAG = "DialogHandler"

    private val _snackbarFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    val snackbarFlow = _snackbarFlow as StateFlow<String?>

    private val _dialogViewState: MutableState<DialogViewState> = mutableStateOf(DialogViewState.Dismissed)

    val dialogViewState: State<DialogViewState> = _dialogViewState

    fun showSnack(text: String) {
        Log.i(TAG, "showSnack(text=[${text}])")
        _snackbarFlow.value = text
    }

    @JvmStatic
    fun acknowledgeSnack() {
        Log.i(TAG, "acknowledgeSnack()")
        _snackbarFlow.value = null
    }

    fun showDeleteDialog(context: Context, stockName: String) {
        _dialogViewState.value = DialogViewState.ShowDeleteDialog(
            stockEventRepository = StockEventRepositoryImpl(context),
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

    sealed class DialogViewState {

        data object Dismissed : DialogViewState()

        data class ShowDeleteDialog(
            val stockEventRepository: StockEventRepository,
            val stockName: String
        ) : DialogViewState(), StockRemoveClickListener {

            override fun onClickRemove(context: Context, stockSymbol: String) {
                stockEventRepository.remove(stockSymbol)
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

    }

}
