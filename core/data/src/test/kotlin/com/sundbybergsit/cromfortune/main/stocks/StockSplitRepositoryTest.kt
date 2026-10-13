package com.sundbybergsit.cromfortune.main.stocks

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.domain.StockSplit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class StockSplitRepositoryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val portfolio = "StockSplitRepositoryTest"
    private lateinit var repository: StockSplitRepository

    @Before
    fun setUp() {
        context.getSharedPreferences("$portfolio-splits", Context.MODE_PRIVATE).edit().clear().commit()
        repository = StockSplitRepository(context, portfolio)
    }

    @Test
    fun `update replaces an existing split`() {
        val original = StockSplit(false, 1L, "TEST", 2)
        val updated = original.copy(reverse = true, dateInMillis = 2L, quantity = 4)
        repository.putReplacingAll(original.name, original)

        repository.update(original, updated)

        assertEquals(setOf(updated), repository.list("TEST"))
    }

    @Test
    fun `remove deletes an existing split`() {
        val split = StockSplit(false, 1L, "TEST", 2)
        repository.putReplacingAll(split.name, split)

        repository.remove(split)

        assertTrue(repository.list("TEST").isEmpty())
    }

    @Test(expected = IllegalArgumentException::class)
    fun `update rejects a missing original split`() {
        repository.update(
            StockSplit(false, 1L, "TEST", 2),
            StockSplit(true, 2L, "TEST", 3)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `putAll rejects split when subsequent sale exceeds available quantity`() {
        val transactionRepository = AssetTransactionRepository(context, portfolio)
        val buy = com.sundbybergsit.cromfortune.domain.AssetTransaction(
            "stock:TEST", com.sundbybergsit.cromfortune.domain.AssetType.STOCK, "TEST", "Test Stock", "SEK",
            com.sundbybergsit.cromfortune.domain.TransactionAction.BUY, 10L, java.math.BigDecimal("10"), java.math.BigDecimal.ZERO, java.math.BigDecimal("100")
        )
        val sell = com.sundbybergsit.cromfortune.domain.AssetTransaction(
            "stock:TEST", com.sundbybergsit.cromfortune.domain.AssetType.STOCK, "TEST", "Test Stock", "SEK",
            com.sundbybergsit.cromfortune.domain.TransactionAction.SELL, 30L, java.math.BigDecimal("10"), java.math.BigDecimal.ZERO, java.math.BigDecimal("50")
        )
        transactionRepository.putAll("stock:TEST", setOf(buy, sell))

        val invalidReverseSplit = StockSplit(reverse = true, dateInMillis = 20L, name = "TEST", quantity = 10)
        repository.putAll("TEST", setOf(invalidReverseSplit))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `remove rejects removing split when remaining transactions and splits lead to negative balance`() {
        val transactionRepository = AssetTransactionRepository(context, portfolio)
        val buy = com.sundbybergsit.cromfortune.domain.AssetTransaction(
            "stock:TEST", com.sundbybergsit.cromfortune.domain.AssetType.STOCK, "TEST", "Test Stock", "SEK",
            com.sundbybergsit.cromfortune.domain.TransactionAction.BUY, 10L, java.math.BigDecimal("10"), java.math.BigDecimal.ZERO, java.math.BigDecimal("50")
        )
        val sell = com.sundbybergsit.cromfortune.domain.AssetTransaction(
            "stock:TEST", com.sundbybergsit.cromfortune.domain.AssetType.STOCK, "TEST", "Test Stock", "SEK",
            com.sundbybergsit.cromfortune.domain.TransactionAction.SELL, 30L, java.math.BigDecimal("10"), java.math.BigDecimal.ZERO, java.math.BigDecimal("80")
        )
        transactionRepository.putAll("stock:TEST", setOf(buy, sell))

        val split = StockSplit(reverse = false, dateInMillis = 20L, name = "TEST", quantity = 2)
        repository.putAll("TEST", setOf(split))

        repository.remove(split)
    }
}
