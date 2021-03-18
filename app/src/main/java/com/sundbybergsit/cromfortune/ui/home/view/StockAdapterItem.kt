package com.sundbybergsit.cromfortune.ui.home.view

import com.sundbybergsit.cromfortune.stocks.StockOrder
import com.sundbybergsit.cromfortune.ui.AdapterItem

data class StockAdapterItem(val stockOrder: StockOrder) : AdapterItem {

    override fun isContentTheSame(item: AdapterItem): Boolean {
        return item is StockAdapterItem && stockOrder == item.stockOrder
    }

}
