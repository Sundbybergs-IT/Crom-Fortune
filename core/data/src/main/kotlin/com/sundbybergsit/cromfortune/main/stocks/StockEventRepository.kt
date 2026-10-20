package com.sundbybergsit.cromfortune.main.stocks

import android.content.Context
import android.util.Log
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockEventApi
import com.sundbybergsit.cromfortune.domain.StockOrderApi
import com.sundbybergsit.cromfortune.domain.StockSplitApi
import com.sundbybergsit.cromfortune.main.Taggable

class StockEventRepository(
    context: Context,
    portfolioName: String,
    private val stockOrderApi: StockOrderApi = StockOrderRepository(context, portfolioName = portfolioName),
    private val stockSplitApi: StockSplitApi = StockSplitRepository(context, porfolioName = portfolioName)
) : StockEventApi, Taggable {

    override fun countCurrent(stockSymbol: String): Int {
        val stockEvents = list(stockSymbol).toMutableList().sortedBy { event -> event.dateInMillis }
        var count = 0
        for (event in stockEvents) {
            event.stockOrder?.let {
                when (it.orderAction) {
                    "Buy" -> count += it.quantity.intValueExact()
                    "Sell" -> count -= it.quantity.intValueExact()
                    "Dividend" -> { /* Dividends do not alter stock count */ }
                    else -> error("Illegal order action: [${it.orderAction}]")
                }
            }
            event.stockSplit?.let {
                if (it.reverse) {
                    count /= it.quantity
                } else {
                    count *= it.quantity
                }
            }
        }
        return count
    }

    override fun listOfStockNames(): Iterable<String> {
        return stockOrderApi.listOfAssetNames()
    }

    override fun isEmpty(): Boolean {
        return stockOrderApi.isEmpty()
    }

    override fun list(stockName: String): List<StockEvent> {
        return stockOrderApi.list(stockName).map { StockEvent(it, null, it.dateInMillis) } +
            stockSplitApi.list(stockName).map { StockEvent(null, it, it.dateInMillis) }
    }

    override fun remove(stockName: String) {
        Log.i(TAG, "remove([$stockName])")
        stockOrderApi.remove(stockName)
        stockSplitApi.remove(stockName)
    }

}
