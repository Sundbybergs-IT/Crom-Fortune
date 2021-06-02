package com.sundbybergsit.cromfortune.ui.home

import com.sundbybergsit.cromfortune.settings.StockMuteSettingsRepository
import com.sundbybergsit.cromfortune.stocks.StockPriceRepository
import com.sundbybergsit.cromfortune.ui.home.view.NameAndValueAdapterItem

internal object StockAggregateAdapterItemUtil {

    @JvmStatic
    fun convertToAdapterItems(list: Iterable<StockOrderAggregate>): List<NameAndValueAdapterItem> {
        val result: MutableList<NameAndValueAdapterItem> = ArrayList()
        result.add(StockAggregateHeaderAdapterItem(list.toSet()))
        for (stockOrderAggregate in list) {
            val pdAdapterItem = StockAggregateAdapterItem(
                stockOrderAggregate = stockOrderAggregate,
                muted = StockMuteSettingsRepository.isMuted(stockOrderAggregate.stockSymbol),
                stockPriceListener = StockPriceRepository
            )
            result.add(pdAdapterItem)
        }
        return result
    }

}
