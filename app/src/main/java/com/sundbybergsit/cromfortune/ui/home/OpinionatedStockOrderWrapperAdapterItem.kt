package com.sundbybergsit.cromfortune.ui.home

data class OpinionatedStockOrderWrapperAdapterItem(val opinionatedStockOrderWrapper: OpinionatedStockOrderWrapper) : AdapterItem {

    override fun isContentTheSame(item: AdapterItem): Boolean {
        return item is OpinionatedStockOrderWrapperAdapterItem && opinionatedStockOrderWrapper == item.opinionatedStockOrderWrapper
    }

}
