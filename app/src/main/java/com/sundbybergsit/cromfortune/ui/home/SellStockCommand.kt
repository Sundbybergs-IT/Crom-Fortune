package com.sundbybergsit.cromfortune.ui.home

import android.content.Context

class SellStockCommand(private val context : Context, private val name : String, private val pricePerStock : Double,
                       private val quantity : Int) : Command {

    override fun execute() {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "Buy: $quantity of $name at price $pricePerStock"
    }

}
