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

    override fun list(assetId: String): List<AssetTransaction> {
        val entities = dao.getTransactionsForAsset(portfolioName, assetId)
        return entities.map { it.toDomain() }
    }

    fun validate(assetId: String, transactions: Collection<AssetTransaction>) {
        require(transactions.all { transaction -> transaction.assetId == assetId }) {
            "Every transaction must match storage key $assetId"
        }
        validateChronologicalBalance(transactions)
    }

    override fun putAll(assetId: String, transactions: Collection<AssetTransaction>) {
        val sortedTransactions = transactions.sortedWith(
            compareBy<AssetTransaction>(
                { it.dateInMillis },
                { if (it.action == TransactionAction.BUY || it.action == TransactionAction.DIVIDEND) 0 else 1 }
            )
        )
        validate(assetId, sortedTransactions)
        dao.deleteForAsset(portfolioName, assetId)
        val entities = sortedTransactions.map { AssetTransactionEntity.fromDomain(portfolioName, it) }
        dao.insertAll(entities)
    }

    override fun putReplacingAll(assetId: String, transaction: AssetTransaction) =
        putAll(assetId, setOf(transaction))

    override fun remove(assetId: String) {
        dao.deleteForAsset(portfolioName, assetId)
    }

    override fun remove(transaction: AssetTransaction) {
        val remaining = list(transaction.assetId).toMutableList()
        val index = remaining.indexOfFirst { it.isSameTransaction(transaction) }
        if (index != -1) {
            remaining.removeAt(index)
        }
        if (remaining.isEmpty()) remove(transaction.assetId) else putAll(transaction.assetId, remaining)
    }

    override fun update(original: AssetTransaction, updated: AssetTransaction) {
        val originalTransactions = list(original.assetId).toMutableList()
        val index = originalTransactions.indexOfFirst { it.isSameTransaction(original) }
        if (index == -1) {
            if (list(updated.assetId).any { it.isSameTransaction(updated) }) return
            throw IllegalArgumentException("Transaction to update no longer exists")
        }

        if (original.assetId == updated.assetId) {
            originalTransactions[index] = updated
            putAll(original.assetId, originalTransactions)
            return
        }

        originalTransactions.removeAt(index)
        val updatedTransactions = list(updated.assetId).toMutableList()
        val updatedIndex = updatedTransactions.indexOfFirst { it.isSameTransaction(updated) }
        if (updatedIndex != -1) {
            updatedTransactions[updatedIndex] = updated
        } else {
            updatedTransactions.add(updated)
        }
        validateChronologicalBalance(originalTransactions)
        validateChronologicalBalance(updatedTransactions)

        if (originalTransactions.isEmpty()) {
            remove(original.assetId)
        } else {
            putAll(original.assetId, originalTransactions)
        }
        putAll(updated.assetId, updatedTransactions)
    }

    private fun validateChronologicalBalance(transactions: Collection<AssetTransaction>) {
        if (transactions.isEmpty()) return
        val first = transactions.first()
        val symbol = first.symbol
        val splits = stockSplitApi.list(symbol)
        ChronologicalBalanceValidator.validate(transactions, splits)
    }
}
