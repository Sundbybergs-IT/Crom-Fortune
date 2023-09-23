package com.sundbybergsit.cromfortune.algorithm

import java.util.Date

data class Recommendation(val command: StockOrderCommand, val date: Date = Date())
