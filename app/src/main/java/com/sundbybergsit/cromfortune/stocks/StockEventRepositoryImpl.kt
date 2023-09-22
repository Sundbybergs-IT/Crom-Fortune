package com.sundbybergsit.cromfortune.stocks

import android.content.Context
import android.os.Build
import android.util.Log
import com.sundbybergsit.cromfortune.Taggable
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockEventRepository
import com.sundbybergsit.cromfortune.domain.StockOrderRepository
import com.sundbybergsit.cromfortune.domain.StockSplitRepository

class StockEventRepositoryImpl(
    context: Context,
    private val stockOrderRepository: StockOrderRepository = StockOrderRepositoryImpl(context),
    private val stockSplitRepository: StockSplitRepository = StockSplitRepositoryImpl(context)
) : StockEventRepository, Taggable {

    override fun listOfStockNames(): Iterable<String> {
        return stockOrderRepository.listOfStockNames()
    }

    override fun isEmpty(): Boolean {
        return stockOrderRepository.isEmpty()
    }

    override fun list(stockName: String): Set<StockEvent> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            (stockOrderRepository.list(stockName).stream().map { StockEvent(it, null, it.dateInMillis) }
                .toList() +
                stockSplitRepository.list(stockName).stream().map { StockEvent(null, it, it.dateInMillis) }.toList())
                .toSet()
        } else {
            mutableSetOf<StockEvent>().apply {
                addAll(stockOrderRepository.list(stockName).map { StockEvent(it, null, it.dateInMillis) })
                addAll(stockSplitRepository.list(stockName).map { StockEvent(null, it, it.dateInMillis) })
            }
        }
    }

    override fun remove(stockName: String) {
        Log.i(TAG, "remove([$stockName])")
        stockOrderRepository.remove(stockName)
        stockSplitRepository.remove(stockName)
    }

}
