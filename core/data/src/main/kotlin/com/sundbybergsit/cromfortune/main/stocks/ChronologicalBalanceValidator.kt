package com.sundbybergsit.cromfortune.main.stocks

import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.StockSplit
import com.sundbybergsit.cromfortune.domain.TransactionAction
import java.math.BigDecimal
import java.math.RoundingMode

object ChronologicalBalanceValidator {

    fun validate(transactions: Set<AssetTransaction>, splits: Set<StockSplit>) {
        if (transactions.isEmpty()) return

        val allEvents = (transactions.map { Triple(it.dateInMillis, it.quantity, it.action as Any) } +
            splits.map { Triple(it.dateInMillis, it.quantity.toBigDecimal(), if (it.reverse) "REVERSE_SPLIT" else "SPLIT") })
            .sortedWith(compareBy(
                { it.first },
                {
                    val type = it.third
                    if (type == TransactionAction.BUY || type == TransactionAction.DIVIDEND || type == "SPLIT" || type == "REVERSE_SPLIT") 0 else 1
                }
            ))

        var balance = BigDecimal.ZERO
        for (event in allEvents) {
            when (event.third) {
                TransactionAction.BUY -> {
                    balance += event.second
                }
                TransactionAction.DIVIDEND -> {
                    // Dividends do not alter stock quantity
                }
                "SPLIT" -> {
                    balance = balance.multiply(event.second)
                }
                "REVERSE_SPLIT" -> {
                    balance = balance.divide(event.second, 16, RoundingMode.DOWN)
                }
                TransactionAction.SELL -> {
                    balance -= event.second
                    require(balance >= BigDecimal.ZERO) { "Sale exceeds available quantity" }
                }
            }
        }
    }

}
