package com.sundbybergsit.cromfortune.algorithm

import com.sundbybergsit.cromfortune.domain.StockOrderRepository

interface Command {

    fun execute(repository : StockOrderRepository)

}
