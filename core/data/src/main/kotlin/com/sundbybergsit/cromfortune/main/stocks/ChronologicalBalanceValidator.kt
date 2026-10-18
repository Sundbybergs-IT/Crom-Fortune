package com.sundbybergsit.cromfortune.main.stocks

import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.StockSplit
import com.sundbybergsit.cromfortune.domain.TransactionAction
import java.math.BigDecimal
import java.math.RoundingMode

object ChronologicalBalanceValidator {

    fun ensureChronologicalBalance(
        transactions: Collection<AssetTransaction>,
        splits: Collection<StockSplit> = emptyList()
    ): List<AssetTransaction> {
        if (transactions.isEmpty()) return emptyList()
        val mutableTransactions = transactions.toMutableList()
        val splitsBySymbol = splits.groupBy { it.name }

        val assetIds = transactions.map { it.assetId }.distinct()
        for (assetId in assetIds) {
            var changed = true
            while (changed) {
                changed = false
                val currentAssetTransactions = mutableTransactions.filter { it.assetId == assetId }
                if (currentAssetTransactions.isEmpty()) continue
                val symbol = currentAssetTransactions.first().symbol
                val assetSplits = splitsBySymbol[symbol] ?: emptyList()

                val allEvents = (currentAssetTransactions.map { Triple(it.dateInMillis, it.quantity, it as Any) } +
                    assetSplits.map {
                        Triple(
                            it.dateInMillis,
                            it.quantity.toBigDecimal(),
                            if (it.reverse) "REVERSE_SPLIT" else "SPLIT" as Any
                        )
                    })
                    .sortedWith(
                        compareBy(
                            { it.first },
                            {
                                val obj = it.third
                                when (obj) {
                                    "SPLIT", "REVERSE_SPLIT" -> -1
                                    is AssetTransaction -> when (obj.action) {
                                        TransactionAction.BUY -> 0
                                        TransactionAction.DIVIDEND -> 1
                                        TransactionAction.SELL -> 2
                                    }
                                    else -> 0
                                }
                            }
                        ))

                var balance = BigDecimal.ZERO
                for (event in allEvents) {
                    when (val obj = event.third) {
                        is AssetTransaction -> {
                            when (obj.action) {
                                TransactionAction.BUY -> balance += obj.quantity
                                TransactionAction.DIVIDEND -> {}
                                TransactionAction.SELL -> {
                                    if (balance < obj.quantity) {
                                        val deficit = obj.quantity - balance
                                        val firstEventDate = allEvents.firstOrNull()?.first ?: obj.dateInMillis
                                        val syntheticBuy = AssetTransaction(
                                            assetId = obj.assetId,
                                            assetType = obj.assetType,
                                            symbol = obj.symbol,
                                            displayName = obj.displayName,
                                            quoteCurrencyCode = obj.quoteCurrency.currencyCode,
                                            action = TransactionAction.BUY,
                                            dateInMillis = minOf(obj.dateInMillis, firstEventDate),
                                            unitPrice = BigDecimal.ZERO,
                                            commissionFee = BigDecimal.ZERO,
                                            quantity = deficit
                                        )
                                        mutableTransactions.add(syntheticBuy)
                                        changed = true
                                        break
                                    }
                                    balance -= obj.quantity
                                }
                            }
                        }
                        "SPLIT" -> {
                            balance = balance.multiply(event.second)
                        }
                        "REVERSE_SPLIT" -> {
                            balance = balance.divide(event.second, 16, RoundingMode.DOWN)
                        }
                    }
                }
            }
        }

        return mutableTransactions.groupBy { Triple(it.assetId, it.action, it.dateInMillis) }
            .map { (_, group) ->
                val first = group.first()
                if (first.action == TransactionAction.DIVIDEND) {
                    if (group.size == 1) {
                        first
                    } else {
                        val totalQuantity = group.sumOf { it.quantity }
                        val avgPrice = if (totalQuantity > BigDecimal.ZERO) {
                            group.sumOf { it.quantity * it.unitPrice }.divide(totalQuantity, 8, RoundingMode.HALF_UP)
                                .stripTrailingZeros()
                        } else {
                            first.unitPrice
                        }
                        first.copy(quantity = totalQuantity, unitPrice = avgPrice)
                    }
                } else {
                    val totalQuantity = group.sumOf { it.quantity }
                    val totalCost = group.sumOf { it.quantity * it.unitPrice }
                    val avgPrice = if (totalQuantity > BigDecimal.ZERO) {
                        totalCost.divide(totalQuantity, 8, RoundingMode.HALF_UP).stripTrailingZeros()
                    } else {
                        BigDecimal.ZERO
                    }
                    val totalCommission = group.sumOf { it.commissionFee }
                    first.copy(
                        quantity = totalQuantity,
                        unitPrice = avgPrice,
                        commissionFee = totalCommission
                    )
                }
            }
    }

    fun validate(transactions: Set<AssetTransaction>, splits: Set<StockSplit>) {
        if (transactions.isEmpty()) return

        val allEvents = (transactions.map { Triple(it.dateInMillis, it.quantity, it.action as Any) } +
            splits.map { Triple(it.dateInMillis, it.quantity.toBigDecimal(), if (it.reverse) "REVERSE_SPLIT" else "SPLIT") })
            .sortedWith(compareBy(
                { it.first },
                {
                    val type = it.third
                    when (type) {
                        "SPLIT", "REVERSE_SPLIT" -> -1
                        TransactionAction.BUY -> 0
                        TransactionAction.DIVIDEND -> 1
                        TransactionAction.SELL -> 2
                        else -> 0
                    }
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
