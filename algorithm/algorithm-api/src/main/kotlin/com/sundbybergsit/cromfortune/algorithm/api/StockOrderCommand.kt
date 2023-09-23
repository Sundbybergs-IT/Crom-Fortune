package com.sundbybergsit.cromfortune.algorithm.api

import com.sundbybergsit.cromfortune.domain.StockOrderApi
import java.util.Currency

interface StockOrderCommand : Command<StockOrderApi> {

    fun quantity() : Int

    fun stockSymbol() : String

    fun currency() : Currency

    fun commissionFee() : Double

    fun price() : Double

}
