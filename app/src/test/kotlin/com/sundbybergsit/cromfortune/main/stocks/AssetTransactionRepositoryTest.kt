package com.sundbybergsit.cromfortune.main.stocks

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.StockSplit
import com.sundbybergsit.cromfortune.domain.TransactionAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class AssetTransactionRepositoryTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `fractional crypto transactions use stable key and retain fallback metadata`() {
        val portfolio = "asset-repository-${System.nanoTime()}"
        val repository = AssetTransactionRepository(context, portfolio)
        val transaction = transaction(TransactionAction.BUY, "0.00000001")

        repository.putReplacingAll(transaction.assetId, transaction)

        assertEquals(setOf("crypto:OLD"), repository.assetIds())
        assertEquals(setOf(transaction), repository.list(transaction.assetId))
        assertEquals(BigDecimal("0.00000001"), repository.currentQuantity(transaction.assetId))
    }

    @Test
    fun `repository rejects a sale exceeding the available exact quantity`() {
        val repository = AssetTransactionRepository(context, "asset-oversell-${System.nanoTime()}")
        val buy = transaction(TransactionAction.BUY, "0.1")
        val sell = transaction(TransactionAction.SELL, "0.10000001", 2L)

        assertThrows(IllegalArgumentException::class.java) {
            repository.putAll(buy.assetId, setOf(buy, sell))
        }
    }

    @Test
    fun `repository accepts a sale from shares created by a stock split`() {
        val portfolio = "asset-split-sale-${System.nanoTime()}"
        StockSplitRepository(context, portfolio).putReplacingAll(
            "OLD",
            StockSplit(reverse = false, dateInMillis = 2L, name = "OLD", quantity = 2)
        )
        val repository = AssetTransactionRepository(context, portfolio)
        val buy = stockTransaction(TransactionAction.BUY, "1", 1L)
        val sell = stockTransaction(TransactionAction.SELL, "2", 3L)

        repository.putAll(buy.assetId, setOf(buy, sell))

        assertEquals(setOf(buy, sell), repository.list(buy.assetId))
    }

    private fun transaction(action: TransactionAction, quantity: String, date: Long = 1L) = AssetTransaction(
        assetId = "crypto:OLD",
        assetType = AssetType.CRYPTO,
        symbol = "OLD",
        displayName = "Retired Coin",
        quoteCurrencyCode = "USD",
        action = action,
        dateInMillis = date,
        unitPrice = BigDecimal("10.25"),
        quantity = BigDecimal(quantity)
    )

    private fun stockTransaction(action: TransactionAction, quantity: String, date: Long) = AssetTransaction(
        assetId = "stock:OLD",
        assetType = AssetType.STOCK,
        symbol = "OLD",
        displayName = "Old Corp",
        quoteCurrencyCode = "USD",
        action = action,
        dateInMillis = date,
        unitPrice = BigDecimal("10.25"),
        quantity = BigDecimal(quantity)
    )
}
