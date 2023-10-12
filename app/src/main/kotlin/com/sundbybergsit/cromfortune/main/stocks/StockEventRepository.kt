package com.sundbybergsit.cromfortune.main.stocks

import android.content.Context
import android.os.Build
import android.util.Log
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockEventApi
import com.sundbybergsit.cromfortune.domain.StockOrderApi
import com.sundbybergsit.cromfortune.domain.StockSplitApi
import com.sundbybergsit.cromfortune.main.Taggable

class StockEventRepository(
    context: Context,
    portfolioName : String,
    private val stockOrderApi: StockOrderApi = StockOrderRepository(context, porfolioName = portfolioName),
    private val stockSplitApi: StockSplitApi = StockSplitRepository(context, porfolioName = portfolioName)
) : StockEventApi, Taggable {

    override fun listOfStockNames(): Iterable<String> {
        return stockOrderApi.listOfStockNames()
    }

    override fun isEmpty(): Boolean {
        return stockOrderApi.isEmpty()
    }

    override fun list(stockName: String): Set<StockEvent> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            (stockOrderApi.list(stockName).stream().map { StockEvent(it, null, it.dateInMillis) }
                .toList() +
                stockSplitApi.list(stockName).stream().map { StockEvent(null, it, it.dateInMillis) }.toList())
                .toSet()
        } else {
            mutableSetOf<StockEvent>().apply {
                addAll(stockOrderApi.list(stockName).map { StockEvent(it, null, it.dateInMillis) })
                addAll(stockSplitApi.list(stockName).map { StockEvent(null, it, it.dateInMillis) })
            }
        }
    }

    override fun remove(stockName: String) {
        Log.i(TAG, "remove([$stockName])")
        stockOrderApi.remove(stockName)
        stockSplitApi.remove(stockName)
    }

}
