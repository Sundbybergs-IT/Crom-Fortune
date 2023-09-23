package com.sundbybergsit.cromfortune.algorithm.api

import java.util.Currency

interface StockOrderCommand : Command {

    fun quantity() : Int

    fun stockSymbol() : String

    fun currency() : Currency

    fun commissionFee() : Double

    fun price() : Double

}
