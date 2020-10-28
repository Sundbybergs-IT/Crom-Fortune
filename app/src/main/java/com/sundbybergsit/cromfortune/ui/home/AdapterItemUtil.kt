package com.sundbybergsit.cromfortune.ui.home

internal object AdapterItemUtil {

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
