package com.sundbybergsit.cromfortune.ui.home

internal object StockAggregateAdapterItemUtil {

    @JvmStatic
    fun convertToAdapterItems(list: Iterable<StockOrderAggregate>): List<AdapterItem> {
        val result: MutableList<AdapterItem> = ArrayList()
        result.add(StockAggregateHeaderAdapterItem(list.toSet()))
        for (connection in list) {
            val pdAdapterItem = StockAggregateAdapterItem(connection)
            result.add(pdAdapterItem)
        }
        return result
    }

}
