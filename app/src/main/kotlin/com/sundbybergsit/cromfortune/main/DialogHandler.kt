package com.sundbybergsit.cromfortune.main

import android.content.Context
import android.util.Log
import com.sundbybergsit.cromfortune.domain.AssetCatalog
import com.sundbybergsit.cromfortune.domain.AssetEvent
import com.sundbybergsit.cromfortune.domain.AssetTransactionApi
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.main.settings.StockRetrievalSettings
import com.sundbybergsit.cromfortune.main.stocks.AssetEventRepository
import com.sundbybergsit.cromfortune.main.ui.home.PortfolioItem
import com.sundbybergsit.cromfortune.main.ui.home.view.StockRemoveClickListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object DialogHandler {

    private const val TAG = "DialogHandler"

    private val _snackbarFlow: MutableStateFlow<Pair<String, Pair<String, suspend () -> Unit>?>?> =
        MutableStateFlow(null)
    val snackbarFlow = _snackbarFlow as StateFlow<Pair<String, Pair<String, suspend () -> Unit>?>?>

    private val _dialogViewState: MutableStateFlow<DialogViewState> = MutableStateFlow(DialogViewState.Dismissed)

    val dialogViewState: StateFlow<DialogViewState> = _dialogViewState.asStateFlow()

    fun showSnack(text: String, action: Pair<String, suspend () -> Unit>? = null) {
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
            assetEventApi = AssetEventRepository(context = context, portfolioName = portfolioName),
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
        _dialogViewState.value = DialogViewState.ShowSupportedStocksDialog(
            text = AssetCatalog.stocks.joinToString { asset -> "${asset.displayName} (${asset.symbol})" }
        )
    }

    fun showSupportedCryptocurrenciesDialog() {
        _dialogViewState.value = DialogViewState.ShowSupportedCryptocurrenciesDialog(
            text = AssetCatalog.cryptocurrencies.joinToString { asset -> "${asset.displayName} (${asset.symbol})" }
        )
    }

    fun showBuyStockDialog(stockSymbol: String? = null) {
        _dialogViewState.value = DialogViewState.ShowBuyStockDialog(stockSymbol = stockSymbol?.let(::assetId))
    }

    fun showStockEvents(stockSymbol: String, stockEvents: List<StockEvent>, readOnly: Boolean) {
        Log.d(TAG, "showStockEvents(stockSymbol=[$stockSymbol], events=${stockEvents.size}, readOnly=$readOnly)")
        _dialogViewState.value = DialogViewState.ShowStockEvents(
            title = "${
                AssetCatalog.findBySymbol(AssetType.STOCK, stockSymbol)?.displayName ?: stockSymbol
            } ($stockSymbol)", stockEvents = stockEvents, readOnly = readOnly
        )
    }

    fun showSellStockDialog(stockSymbol: String? = null) {
        _dialogViewState.value = DialogViewState.ShowSellStockDialog(stockSymbol = stockSymbol?.let(::assetId))
    }

    fun showSplitStockDialog(stockSymbol: String? = null) {
        _dialogViewState.value = DialogViewState.ShowRegisterSplitStockDialog(stockSymbol = stockSymbol)
    }

    fun showAddPortfolioDialog() {
        _dialogViewState.value = DialogViewState.ShowAddPortfolio
    }

    fun showAssetEvents(item: PortfolioItem, transactionApi: AssetTransactionApi, readOnly: Boolean) {
        _dialogViewState.value = DialogViewState.ShowAssetEvents(
            title = item.displayName,
            events = item.assetEvents,
            transactionApi = transactionApi,
            readOnly = readOnly
        )
    }

    private fun assetId(symbolOrId: String): String = if (symbolOrId.contains(':')) {
        symbolOrId
    } else {
        AssetCatalog.findBySymbol(AssetType.STOCK, symbolOrId)?.id ?: "stock:$symbolOrId"
    }

    sealed class DialogViewState {

        data object Dismissed : DialogViewState()

        data class ShowDeleteDialog(
            val assetEventApi: com.sundbybergsit.cromfortune.domain.AssetEventApi,
            val stockName: String
        ) : DialogViewState(), StockRemoveClickListener {

            override fun onClickRemove(context: Context, stockSymbol: String) {
                assetEventApi.remove(stockSymbol)
            }

        }

        class ShowStockRetrievalTimeIntervalsDialog(val stockRetrievalSettings: StockRetrievalSettings) :
            DialogViewState() {

        }

        data class ShowSupportedStocksDialog(val text: String) : DialogViewState()

        data class ShowSupportedCryptocurrenciesDialog(val text: String) : DialogViewState()

        data class ShowStockEvents(val title: String, val stockEvents: List<StockEvent>, val readOnly: Boolean) :
            DialogViewState()

        data class ShowAssetEvents(
            val title: String,
            val events: List<AssetEvent>,
            val transactionApi: AssetTransactionApi,
            val readOnly: Boolean
        ) :
            DialogViewState()

        data class ShowBuyStockDialog(val stockSymbol: String? = null) : DialogViewState()

        data class ShowSellStockDialog(val stockSymbol: String? = null) : DialogViewState()

        data class ShowRegisterSplitStockDialog(val stockSymbol: String? = null) : DialogViewState()

        data object ShowAddPortfolio : DialogViewState()

    }

}
