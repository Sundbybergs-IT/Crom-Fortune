package com.sundbybergsit.cromfortune

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.sundbybergsit.cromfortune.domain.StockEventRepository
import com.sundbybergsit.cromfortune.settings.StockRetrievalSettings
import com.sundbybergsit.cromfortune.stocks.StockEventRepositoryImpl
import com.sundbybergsit.cromfortune.ui.home.view.StockRemoveClickListener
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

    }

}
