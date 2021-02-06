package com.sundbybergsit.cromfortune.ui.home

import com.sundbybergsit.cromfortune.stocks.StockOrder

internal object StockAdapterItemUtil {

    @JvmStatic
    fun convertToAdapterItems(list: Iterable<StockOrder>): List<AdapterItem> {
        val result: MutableList<AdapterItem> = ArrayList()
        result.add(StockHeaderAdapterItem())
        for (connection in list) {
            val pdAdapterItem = StockAdapterItem(connection)
            result.add(pdAdapterItem)
        }
        return result
    }

}
