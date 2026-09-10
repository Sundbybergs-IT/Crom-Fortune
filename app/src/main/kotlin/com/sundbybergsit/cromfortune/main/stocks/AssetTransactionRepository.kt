package com.sundbybergsit.cromfortune.main.stocks

import android.content.Context
import android.content.SharedPreferences
import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.AssetTransactionApi
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.StockOrder
import com.sundbybergsit.cromfortune.domain.StockSplitApi
import com.sundbybergsit.cromfortune.domain.TransactionAction
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.math.BigDecimal

class AssetTransactionRepository(
    context: Context,
    portfolioName: String,
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(portfolioName, Context.MODE_PRIVATE),
    private val stockSplitApi: StockSplitApi = StockSplitRepository(context, porfolioName = portfolioName)
) : AssetTransactionApi {

    override fun currentQuantity(assetId: String): BigDecimal = list(assetId).fold(BigDecimal.ZERO) { quantity, transaction ->
        when (transaction.action) {
            TransactionAction.BUY -> quantity + transaction.quantity
            TransactionAction.SELL -> quantity - transaction.quantity
        }
    }

    override fun assetIds(): Set<String> = sharedPreferences.all.keys.mapTo(mutableSetOf()) { key ->
        if (key.contains(':')) key else "stock:$key"
    }

    override fun isEmpty(): Boolean = sharedPreferences.all.isEmpty()

    override fun list(assetId: String): Set<AssetTransaction> =
        (sharedPreferences.getStringSet(assetId, null)
            ?: assetId.takeIf { it.startsWith("stock:") }
                ?.removePrefix("stock:")
                ?.let { legacyKey -> sharedPreferences.getStringSet(legacyKey, emptySet()) }
            ?: emptySet()).flatMapTo(mutableSetOf()) { serializedSet ->
            decodeTransactions(serializedSet)
        }

    override fun putAll(assetId: String, transactions: Set<AssetTransaction>) {
        require(transactions.all { transaction -> transaction.assetId == assetId }) {
            "Every transaction must match storage key $assetId"
        }
        validateChronologicalBalance(transactions)
        check(sharedPreferences.edit().putStringSet(assetId, setOf(Json.encodeToString(transactions))).commit()) {
            "Failed to persist transactions for $assetId"
        }
    }

    override fun putReplacingAll(assetId: String, transaction: AssetTransaction) =
        putAll(assetId, setOf(transaction))

    override fun remove(assetId: String) {
        check(sharedPreferences.edit().remove(assetId).commit()) { "Failed to remove transactions for $assetId" }
    }

    override fun remove(transaction: AssetTransaction) {
        val remaining = list(transaction.assetId) - transaction
        if (remaining.isEmpty()) remove(transaction.assetId) else putAll(transaction.assetId, remaining)
    }

    private fun validateChronologicalBalance(transactions: Set<AssetTransaction>) {
        var quantity = BigDecimal.ZERO
        val assetId = transactions.firstOrNull()?.assetId.orEmpty()
        val stockSplits = transactions.firstOrNull()
            ?.takeIf { it.assetType == AssetType.STOCK }
            ?.let { stockSplitApi.list(it.symbol) }
            .orEmpty()
        val events = transactions.map { transaction ->
            transaction.dateInMillis to { quantity = when (transaction.action) {
                TransactionAction.BUY -> quantity + transaction.quantity
                TransactionAction.SELL -> quantity - transaction.quantity
            } }
        } + stockSplits.map { split ->
            split.dateInMillis to {
                quantity = if (split.reverse) {
                    quantity.divideToIntegralValue(split.quantity.toBigDecimal())
                } else {
                    quantity.multiply(split.quantity.toBigDecimal())
                }
            }
        }
        events.sortedBy { it.first }.forEach { (_, applyEvent) ->
            applyEvent()
            require(quantity >= BigDecimal.ZERO) { "Sale exceeds available quantity for $assetId" }
        }
    }

    private fun decodeTransactions(serializedSet: String): Set<AssetTransaction> = try {
        Json.decodeFromString(serializedSet)
    } catch (_: SerializationException) {
        Json.decodeFromString<Set<StockOrder>>(serializedSet).mapTo(mutableSetOf(), AssetTransaction::fromStockOrder)
    } catch (_: IllegalArgumentException) {
        Json.decodeFromString<Set<StockOrder>>(serializedSet).mapTo(mutableSetOf(), AssetTransaction::fromStockOrder)
    }
}
