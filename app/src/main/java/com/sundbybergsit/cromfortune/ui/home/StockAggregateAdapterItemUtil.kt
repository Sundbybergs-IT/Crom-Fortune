package com.sundbybergsit.cromfortune.ui.home

import com.sundbybergsit.cromfortune.settings.StockMuteSettingsRepository
import com.sundbybergsit.cromfortune.ui.AdapterItem

internal object StockAggregateAdapterItemUtil {

    @JvmStatic
    fun convertToAdapterItems(list: Iterable<StockOrderAggregate>): List<AdapterItem> {
        val result: MutableList<AdapterItem> = ArrayList()
        result.add(StockAggregateHeaderAdapterItem(list.toSet()))
        for (stockOrderAggregate in list) {
            val pdAdapterItem = StockAggregateAdapterItem(stockOrderAggregate = stockOrderAggregate,
                    muted = StockMuteSettingsRepository.isMuted(stockOrderAggregate.stockSymbol))
            result.add(pdAdapterItem)
        }
        return result
    }

}
