package com.sundbybergsit.cromfortune.ui.home

import com.sundbybergsit.cromfortune.ui.AdapterItem
import com.sundbybergsit.cromfortune.ui.home.view.NameAndValueAdapterItem

data class StockAggregateHeaderAdapterItem(val stockOrderAggregates: Set<StockOrderAggregate>) :
    NameAndValueAdapterItem("dummy value", 0.0) {

    override fun isContentTheSame(item: AdapterItem): Boolean {
        return item is StockAggregateHeaderAdapterItem && stockOrderAggregates == item.stockOrderAggregates
    }

}
