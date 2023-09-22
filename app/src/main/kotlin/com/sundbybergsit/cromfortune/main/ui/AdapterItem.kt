package com.sundbybergsit.cromfortune.main.ui

interface AdapterItem {

    fun isContentTheSame(item: com.sundbybergsit.cromfortune.main.ui.AdapterItem): Boolean {
        return item::class.java == this::class.java && this == item
    }

}
