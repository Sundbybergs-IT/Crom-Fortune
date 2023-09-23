package com.sundbybergsit.cromfortune.algorithm.api

import com.sundbybergsit.cromfortune.domain.StockOrderRepository

interface Command {

    fun execute(repository : StockOrderRepository)

}
