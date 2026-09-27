package com.sundbybergsit.cromfortune.main.stocks

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.StockSplit
import com.sundbybergsit.cromfortune.domain.TransactionAction
import kotlinx.serialization.json.Json
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

    @Test
    fun `removing the last legacy stock transaction removes its legacy key`() {
        val portfolio = "asset-legacy-remove-${System.nanoTime()}"
        val transaction = stockTransaction(TransactionAction.BUY, "1", 1L)
        context.getSharedPreferences(portfolio, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(transaction.symbol, setOf(Json.encodeToString(setOf(transaction))))
            .commit()
        val repository = AssetTransactionRepository(context, portfolio)

        assertEquals(setOf(transaction), repository.list(transaction.assetId))

        repository.remove(transaction)

        assertEquals(emptySet<AssetTransaction>(), repository.list(transaction.assetId))
        assertEquals(emptySet<String>(), repository.assetIds())
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

        assertEquals(setOf(earlierBuy, updated), repository.list(original.assetId))
    }

    @Test
    fun `update can move a transaction to another asset`() {
        val repository = AssetTransactionRepository(context, "asset-update-move-${System.nanoTime()}")
        val original = stockTransaction(TransactionAction.BUY, "2", 1L)
        val updated = transaction(TransactionAction.BUY, "0.5", 2L)
        repository.putReplacingAll(original.assetId, original)

        repository.update(original, updated)

        assertEquals(emptySet<AssetTransaction>(), repository.list(original.assetId))
        assertEquals(setOf(updated), repository.list(updated.assetId))
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
