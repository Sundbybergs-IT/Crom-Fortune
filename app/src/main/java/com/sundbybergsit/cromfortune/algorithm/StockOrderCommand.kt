package com.sundbybergsit.cromfortune.algorithm

import java.util.*

interface StockOrderCommand : Command {

    fun quantity() : Int

    fun stockSymbol() : String

    fun currency() : Currency

    fun commissionFee() : Double

    fun price() : Double

}
