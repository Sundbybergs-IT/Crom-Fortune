package com.sundbybergsit.cromfortune.stocks

import android.content.Context
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockEventRepository
import com.sundbybergsit.cromfortune.domain.StockOrderRepository
import com.sundbybergsit.cromfortune.domain.StockSplitRepository
import kotlin.streams.toList

class StockEventRepositoryImpl(
    context: Context,
    private val stockOrderRepository: StockOrderRepository = StockOrderRepositoryImpl(context),
    private val stockSplitRepository: StockSplitRepository = StockSplitRepositoryImpl(context)
) : StockEventRepository {

    override fun listOfStockNames(): Iterable<String> {
        return stockOrderRepository.listOfStockNames()
    }

    override fun isEmpty(): Boolean {
        return stockOrderRepository.isEmpty()
    }

    override fun list(stockName: String): Set<StockEvent> {
        return (stockOrderRepository.list(stockName).stream().map { StockEvent(it, null, it.dateInMillis) }.toList() +
                stockSplitRepository.list(stockName).stream().map { StockEvent(null, it, it.dateInMillis) }.toList())
            .toSet()
    }

    override fun remove(stockName: String) {
        stockOrderRepository.remove(stockName)
        stockSplitRepository.remove(stockName)
    }

}
