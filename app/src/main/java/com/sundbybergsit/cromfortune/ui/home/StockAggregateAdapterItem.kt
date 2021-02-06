package com.sundbybergsit.cromfortune.ui.home

data class StockAggregateAdapterItem(val stockOrderAggregate: StockOrderAggregate) : AdapterItem {

    override fun isContentTheSame(item: AdapterItem): Boolean {
        return item is StockAggregateAdapterItem && stockOrderAggregate == item.stockOrderAggregate
    }

}
