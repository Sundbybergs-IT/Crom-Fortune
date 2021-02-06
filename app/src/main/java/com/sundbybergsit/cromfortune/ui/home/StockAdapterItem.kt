package com.sundbybergsit.cromfortune.ui.home

import com.sundbybergsit.cromfortune.stocks.StockOrder

data class StockAdapterItem(val stockOrder: StockOrder) : AdapterItem {

    override fun isContentTheSame(item: AdapterItem): Boolean {
        return item is StockAdapterItem && stockOrder == item.stockOrder
    }

}
