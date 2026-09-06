package com.sundbybergsit.cromfortune.algorithm.core

import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.AssetTransactionApi
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.TransactionAction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class AssetCommandTest {

    @Test
    fun `buy command preserves fractional quantity and exact price`() {
        val repository = InMemoryAssetTransactionApi()
        BuyAssetCommand(
            assetId = "crypto:BTC",
            assetType = AssetType.CRYPTO,
            symbol = "BTC",
            displayName = "Bitcoin",
            quoteCurrencyCode = "USD",
            dateInMillis = 1L,
            unitPrice = BigDecimal("60123.456789"),
            quantity = BigDecimal("0.00000001")
        ).execute(repository)

        val saved = repository.list("crypto:BTC").single()
        assertEquals(BigDecimal("0.00000001"), saved.quantity)
        assertEquals(BigDecimal("60123.456789"), saved.unitPrice)
    }

    @Test
    fun `sell command rejects quantity above exact holding`() {
        val repository = InMemoryAssetTransactionApi()
        val command = SellAssetCommand(
            assetId = "crypto:BTC",
            assetType = AssetType.CRYPTO,
            symbol = "BTC",
            displayName = "Bitcoin",
            quoteCurrencyCode = "USD",
            dateInMillis = 2L,
            unitPrice = BigDecimal("61000"),
            quantity = BigDecimal("0.1")
        )

        assertThrows(IllegalArgumentException::class.java) { command.execute(repository) }
    }

    private class InMemoryAssetTransactionApi : AssetTransactionApi {
        private val values = mutableMapOf<String, Set<AssetTransaction>>()
        override fun currentQuantity(assetId: String): BigDecimal =
            values[assetId].orEmpty().fold(BigDecimal.ZERO) { total, transaction ->
                if (transaction.action == TransactionAction.BUY) total + transaction.quantity else total - transaction.quantity
            }
        override fun assetIds(): Set<String> = values.keys
        override fun isEmpty(): Boolean = values.isEmpty()
        override fun list(assetId: String): Set<AssetTransaction> = values[assetId].orEmpty()
        override fun putAll(assetId: String, transactions: Set<AssetTransaction>) { values[assetId] = transactions }
        override fun putReplacingAll(assetId: String, transaction: AssetTransaction) { values[assetId] = setOf(transaction) }
        override fun remove(assetId: String) { values.remove(assetId) }
        override fun remove(transaction: AssetTransaction) { values[transaction.assetId] = list(transaction.assetId) - transaction }
    }
}
