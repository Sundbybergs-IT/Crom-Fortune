package com.sundbybergsit.cromfortune.algorithm.api

import java.util.Date

data class Recommendation(val command: StockOrderCommand, val date: Date = Date())
