package com.sundbybergsit.cromfortune.ui.home

import android.content.Context
import java.util.*

class SellStockCommand(private val context: Context, private val currency: Currency, private val name: String,
                       private val pricePerStock: Double, private val quantity: Int) : Command {

    override fun execute() {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return "Buy: $quantity of $name at price ${pricePerStock.roundTo(3)} $currency"
    }

    private fun Double.roundTo(n: Int): Double {
        return String.format(Locale.ENGLISH, "%.${n}f", this).toDouble()
    }

}
