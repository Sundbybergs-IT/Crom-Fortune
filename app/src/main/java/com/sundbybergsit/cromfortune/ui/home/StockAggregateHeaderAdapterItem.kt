package com.sundbybergsit.cromfortune.ui.home

data class StockAggregateHeaderAdapterItem(val stockOrderAggregates: Set<StockOrderAggregate>) : AdapterItem {

    override fun isContentTheSame(item: AdapterItem): Boolean {
        return item is StockAggregateHeaderAdapterItem && stockOrderAggregates == item.stockOrderAggregates
    }

}
