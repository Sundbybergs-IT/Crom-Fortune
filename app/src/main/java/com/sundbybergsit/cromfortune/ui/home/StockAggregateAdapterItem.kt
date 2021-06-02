package com.sundbybergsit.cromfortune.ui.home

import com.sundbybergsit.cromfortune.stocks.StockPriceListener
import com.sundbybergsit.cromfortune.ui.AdapterItem
import com.sundbybergsit.cromfortune.ui.home.view.NameAndValueAdapterItem

data class StockAggregateAdapterItem(
    val stockPriceListener: StockPriceListener,
    val stockOrderAggregate: StockOrderAggregate,
    var muted: Boolean = false,
) : NameAndValueAdapterItem(stockOrderAggregate.displayName, stockOrderAggregate.getProfit(stockPriceListener.getStockPrice(stockOrderAggregate.stockSymbol).price)) {

    override fun isContentTheSame(item: AdapterItem): Boolean {
        return item is StockAggregateAdapterItem && stockOrderAggregate == item.stockOrderAggregate
    }

}
