package com.sundbybergsit.cromfortune.ui.home.view

import com.sundbybergsit.cromfortune.ui.AdapterItem

internal data class OpinionatedStockOrderWrapperAdapterItem(val opinionatedStockOrderWrapper: OpinionatedStockOrderWrapper) : AdapterItem {

    override fun isContentTheSame(item: AdapterItem): Boolean {
        return item is OpinionatedStockOrderWrapperAdapterItem && opinionatedStockOrderWrapper == item.opinionatedStockOrderWrapper
    }

}
