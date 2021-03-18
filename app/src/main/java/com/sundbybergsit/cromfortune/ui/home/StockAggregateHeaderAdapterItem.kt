package com.sundbybergsit.cromfortune.ui.home

import com.sundbybergsit.cromfortune.ui.AdapterItem

data class StockAggregateHeaderAdapterItem(val stockOrderAggregates: Set<StockOrderAggregate>) : AdapterItem {

    override fun isContentTheSame(item: AdapterItem): Boolean {
        return item is StockAggregateHeaderAdapterItem && stockOrderAggregates == item.stockOrderAggregates
    }

}
