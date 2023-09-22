package com.sundbybergsit.cromfortune.main.ui.home.view

import android.content.Context

internal interface StockRemoveClickListener {

    fun onClickRemove(context: Context, stockSymbol : String)

}
