package com.sundbybergsit.cromfortune.main.stocks

import android.content.Context
import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.AssetTransactionApi
import com.sundbybergsit.cromfortune.domain.StockSplitApi
import com.sundbybergsit.cromfortune.domain.TransactionAction
import com.sundbybergsit.cromfortune.main.db.AssetTransactionDao
import com.sundbybergsit.cromfortune.main.db.AssetTransactionEntity
import com.sundbybergsit.cromfortune.main.db.CromFortuneDatabase
import java.math.BigDecimal

class AssetTransactionRepository(
    @Suppress("UNUSED_PARAMETER") context: Context,
    private val portfolioName: String,
    private val dao: AssetTransactionDao = CromFortuneDatabase.getInstance(context).assetTransactionDao(),
    private val stockSplitApi: StockSplitApi = StockSplitRepository(context, porfolioName = portfolioName)
) : AssetTransactionApi {

    override fun currentQuantity(assetId: String): BigDecimal = list(assetId).fold(BigDecimal.ZERO) { quantity, transaction ->
        when (transaction.action) {
            TransactionAction.BUY -> quantity + transaction.quantity
            TransactionAction.SELL -> quantity - transaction.quantity
            TransactionAction.DIVIDEND -> quantity
        }
    }

    override fun assetIds(): Set<String> {
        val list = dao.getTransactionsForPortfolio(portfolioName)
        return list.map { it.assetId }.toMutableSet()
    }

    override fun isEmpty(): Boolean {
        return dao.getTransactionsForPortfolio(portfolioName).isEmpty()
    }

    override fun list(assetId: String): Set<AssetTransaction> {
        val entities = dao.getTransactionsForAsset(portfolioName, assetId)
        return entities.map { it.toDomain() }.toSet()
    }

    override fun putAll(assetId: String, transactions: Set<AssetTransaction>) {
        require(transactions.all { transaction -> transaction.assetId == assetId }) {
            "Every transaction must match storage key $assetId"
        }
        validateChronologicalBalance(transactions)
        dao.deleteForAsset(portfolioName, assetId)
        val entities = transactions.map { AssetTransactionEntity.fromDomain(portfolioName, it) }
        dao.insertAll(entities)
    }

    override fun putReplacingAll(assetId: String, transaction: AssetTransaction) =
        putAll(assetId, setOf(transaction))

    override fun remove(assetId: String) {
        dao.deleteForAsset(portfolioName, assetId)
    }

    override fun remove(transaction: AssetTransaction) {
        val remaining = list(transaction.assetId) - transaction
        if (remaining.isEmpty()) remove(transaction.assetId) else putAll(transaction.assetId, remaining)
    }

    fun update(original: AssetTransaction, updated: AssetTransaction) {
        val originalTransactions = list(original.assetId)
        if (original !in originalTransactions) {
            if (updated in list(updated.assetId)) return
            throw IllegalArgumentException("Transaction to update no longer exists")
        }

        if (original.assetId == updated.assetId) {
            putAll(original.assetId, originalTransactions - original + updated)
            return
        }

        val remainingOriginals = originalTransactions - original
        val updatedTransactions = list(updated.assetId) + updated
        validateChronologicalBalance(remainingOriginals)
        validateChronologicalBalance(updatedTransactions)

        if (remainingOriginals.isEmpty()) {
            remove(original.assetId)
        } else {
            putAll(original.assetId, remainingOriginals)
        }
        putAll(updated.assetId, updatedTransactions)
    }

    private fun validateChronologicalBalance(transactions: Set<AssetTransaction>) {
        if (transactions.isEmpty()) return
        val first = transactions.first()
        val symbol = first.symbol
        val splits = stockSplitApi.list(symbol).sortedBy { it.dateInMillis }
        val allEvents = (transactions.map { Triple(it.dateInMillis, it.quantity, it.action) } +
            splits.map { Triple(it.dateInMillis, it.quantity.toBigDecimal(), if (it.reverse) "REVERSE_SPLIT" else "SPLIT") })
            .sortedWith(compareBy({ it.first }, { if (it.third == TransactionAction.BUY || it.third == "SPLIT" || it.third == "REVERSE_SPLIT") 0 else 1 }))

        var balance = BigDecimal.ZERO
        for (event in allEvents) {
            when (event.third) {
                TransactionAction.BUY -> balance += event.second
                TransactionAction.SELL -> {
                    balance -= event.second
                    require(balance >= BigDecimal.ZERO) { "Negative balance detected" }
                }
                TransactionAction.DIVIDEND -> {}
                "SPLIT" -> {
                    balance = balance.multiply(event.second)
                }
                "REVERSE_SPLIT" -> {
                    balance = balance.divide(event.second, 16, java.math.RoundingMode.DOWN)
                }
            }
        }
    }
}
