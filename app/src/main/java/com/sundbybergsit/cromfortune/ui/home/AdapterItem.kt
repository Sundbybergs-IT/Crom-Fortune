package com.sundbybergsit.cromfortune.ui.home

interface AdapterItem {

    fun isContentTheSame(item: AdapterItem): Boolean {
        return item::class.java == this::class.java && this == item
    }

}
