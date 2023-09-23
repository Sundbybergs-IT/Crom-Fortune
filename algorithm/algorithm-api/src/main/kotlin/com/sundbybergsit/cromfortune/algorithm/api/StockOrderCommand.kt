package com.sundbybergsit.cromfortune.algorithm.api

import com.sundbybergsit.cromfortune.domain.StockOrderRepository
import java.util.Currency

interface StockOrderCommand : Command<StockOrderRepository> {

    fun quantity() : Int

    fun stockSymbol() : String

    fun currency() : Currency

    fun commissionFee() : Double

    fun price() : Double

}
