package com.sundbybergsit.cromfortune.ui.home

data class StockAggregateAdapterItem(
        val stockOrderAggregate: StockOrderAggregate,
        var muted: Boolean = false,
) : AdapterItem {

    override fun isContentTheSame(item: AdapterItem): Boolean {
        return item is StockAggregateAdapterItem && stockOrderAggregate == item.stockOrderAggregate
    }

}
