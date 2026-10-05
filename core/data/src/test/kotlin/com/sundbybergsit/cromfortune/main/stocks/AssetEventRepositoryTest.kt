package com.sundbybergsit.cromfortune.main.stocks

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.TransactionAction
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class AssetEventRepositoryTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `currentQuantity ignores dividends and correctly tracks buy and sell actions`() {
        val portfolio = "asset-event-repository-${System.nanoTime()}"
        val transactionRepository = AssetTransactionRepository(context, portfolio)
        val eventRepository = AssetEventRepository(context, portfolio)

        val buy = stockTransaction(TransactionAction.BUY, "10", 1L)
        val dividend = stockTransaction(TransactionAction.DIVIDEND, "10", 2L)
        val sell = stockTransaction(TransactionAction.SELL, "4", 3L)

        transactionRepository.putAll(buy.assetId, setOf(buy, dividend, sell))

        assertEquals(BigDecimal("6"), eventRepository.currentQuantity(buy.assetId))
    }

    private fun stockTransaction(action: TransactionAction, quantity: String, date: Long) = AssetTransaction(
        assetId = "stock:ASSA-B.ST",
        assetType = AssetType.STOCK,
        symbol = "ASSA-B.ST",
        displayName = "ASSA ABLOY AB (publ)",
        quoteCurrencyCode = "SEK",
        action = action,
        dateInMillis = date,
        unitPrice = BigDecimal("250.00"),
        quantity = BigDecimal(quantity)
    )
}
