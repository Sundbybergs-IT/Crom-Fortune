package com.sundbybergsit.cromfortune.ui.home

data class StockAggregateHeaderAdapteritem(val stockOrderAggregates: Set<StockOrderAggregate>) : AdapterItem {

    override fun isContentTheSame(item: AdapterItem): Boolean {
        return item is StockAggregateHeaderAdapteritem && stockOrderAggregates == item.stockOrderAggregates
    }

}
