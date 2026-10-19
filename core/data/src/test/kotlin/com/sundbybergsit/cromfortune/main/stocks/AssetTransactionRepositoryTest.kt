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
        assertEquals(listOf(transaction), repository.list(transaction.assetId))
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

        assertEquals(listOf(buy, sell), repository.list(buy.assetId))
    }

    @Test
    fun `update replaces every editable value without retaining original`() {
        val repository = AssetTransactionRepository(context, "asset-update-${System.nanoTime()}")
        val earlierBuy = stockTransaction(TransactionAction.BUY, "3", 1L)
        val original = stockTransaction(TransactionAction.BUY, "2", 2L)
        val updated = original.copy(
            action = TransactionAction.SELL,
            dateInMillis = 3L,
            unitPrice = BigDecimal("12.75"),
            commissionFee = BigDecimal("1.25"),
            quantity = BigDecimal("1")
        )
        repository.putAll(original.assetId, setOf(earlierBuy, original))

        repository.update(original, updated)

        assertEquals(listOf(earlierBuy, updated), repository.list(original.assetId))
    }

    @Test
    fun `retrying an already persisted update succeeds`() {
        val repository = AssetTransactionRepository(context, "asset-update-retry-${System.nanoTime()}")
        val original = transaction(TransactionAction.BUY, "0.5")
        val updated = original.copy(unitPrice = BigDecimal("12.75"))
        repository.putReplacingAll(original.assetId, original)
        repository.update(original, updated)

        repository.update(original, updated)

        assertEquals(listOf(updated), repository.list(original.assetId))
    }

    @Test
    fun `update can move a transaction to another asset`() {
        val repository = AssetTransactionRepository(context, "asset-update-move-${System.nanoTime()}")
        val original = stockTransaction(TransactionAction.BUY, "2", 1L)
        val updated = transaction(TransactionAction.BUY, "0.5", 2L)
        repository.putReplacingAll(original.assetId, original)

        repository.update(original, updated)

        assertEquals(emptyList<AssetTransaction>(), repository.list(original.assetId))
        assertEquals(listOf(updated), repository.list(updated.assetId))
        assertEquals(setOf(updated.assetId), repository.assetIds())
    }

    @Test
    fun `invalid update leaves original transaction unchanged`() {
        val repository = AssetTransactionRepository(context, "asset-update-invalid-${System.nanoTime()}")
        val buy = stockTransaction(TransactionAction.BUY, "1", 1L)
        val sell = stockTransaction(TransactionAction.SELL, "1", 2L)
        repository.putAll(buy.assetId, setOf(buy, sell))

        assertThrows(IllegalArgumentException::class.java) {
            repository.update(buy, buy.copy(quantity = BigDecimal("0.5")))
        }

        assertEquals(listOf(buy, sell), repository.list(buy.assetId))
    }

    @Test
    fun `remove - when removing a buy transaction causes a subsequent sell to exceed available quantity - throws exception and leaves transactions intact`() {
        val repository = AssetTransactionRepository(context, "asset-remove-oversell-${System.nanoTime()}")
        val buy1 = stockTransaction(TransactionAction.BUY, "50", 1L)
        val buy2 = stockTransaction(TransactionAction.BUY, "50", 2L)
        val sell = stockTransaction(TransactionAction.SELL, "80", 3L)
        repository.putAll(buy1.assetId, setOf(buy1, buy2, sell))

        assertThrows(IllegalArgumentException::class.java) {
            repository.remove(buy1)
        }

        assertEquals(listOf(buy1, buy2, sell), repository.list(buy1.assetId))
    }

    @Test
    fun `remove - when removing a buy transaction leaves sufficient quantity for subsequent sell - succeeds`() {
        val repository = AssetTransactionRepository(context, "asset-remove-valid-${System.nanoTime()}")
        val buy1 = stockTransaction(TransactionAction.BUY, "50", 1L)
        val buy2 = stockTransaction(TransactionAction.BUY, "50", 2L)
        val sell = stockTransaction(TransactionAction.SELL, "30", 3L)
        repository.putAll(buy1.assetId, setOf(buy1, buy2, sell))

        repository.remove(buy1)

        assertEquals(listOf(buy2, sell), repository.list(buy1.assetId))
    }

    @Test
    fun `remove - handles BigDecimal scale differences correctly`() {
        val repository = AssetTransactionRepository(context, "asset-remove-scale-${System.nanoTime()}")
        val storedBuy = stockTransaction(TransactionAction.BUY, "10", 1L).copy(unitPrice = BigDecimal("10.25000000"))
        repository.putReplacingAll(storedBuy.assetId, storedBuy)

        val targetToRemove = storedBuy.copy(unitPrice = BigDecimal("10.25"))
        repository.remove(targetToRemove)

        assertEquals(emptyList<AssetTransaction>(), repository.list(storedBuy.assetId))
    }

    @Test
    fun `remove - removes only one instance when duplicate transactions exist on same date`() {
        val repository = AssetTransactionRepository(context, "asset-remove-duplicate-${System.nanoTime()}")
        val buy1 = stockTransaction(TransactionAction.BUY, "10", 1L)
        val buy2 = stockTransaction(TransactionAction.BUY, "5", 2L)
        repository.putAll(buy1.assetId, setOf(buy1, buy2))

        repository.remove(buy1)

        assertEquals(listOf(buy2), repository.list(buy1.assetId))
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
