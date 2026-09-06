package com.sundbybergsit.cromfortune.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Currency

class AssetHoldingTest {

    @Test
    fun `fractional crypto transactions retain exact quantity and profit`() {
        val holding = cryptoHolding()
        holding.aggregate(event(transaction(TransactionAction.BUY, "0.12345678", "100.10")))
        holding.aggregate(event(transaction(TransactionAction.SELL, "0.02345678", "120.20", date = 2L)))

        assertEquals(BigDecimal("0.10000000"), holding.quantity)
        assertEquals(0, BigDecimal("1.461481278").compareTo(holding.profit(BigDecimal("110.00"))))
    }

    @Test
    fun `selling more than exact holding is rejected`() {
        val holding = cryptoHolding()
        holding.aggregate(event(transaction(TransactionAction.BUY, "0.1", "100")))

        assertThrows(IllegalArgumentException::class.java) {
            holding.aggregate(event(transaction(TransactionAction.SELL, "0.10000001", "100", date = 2L)))
        }
    }

    @Test
    fun `crypto split event is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            AssetEvent(
                assetId = "crypto:BTC",
                assetType = AssetType.CRYPTO,
                stockSplit = StockSplit(false, 2L, "BTC", 2),
                dateInMillis = 2L
            )
        }
    }

    private fun cryptoHolding() = AssetHolding(
        assetId = "crypto:BTC",
        assetType = AssetType.CRYPTO,
        symbol = "BTC",
        displayName = "Bitcoin",
        quoteCurrency = Currency.getInstance("USD"),
        rateInSek = BigDecimal.ONE
    )

    private fun transaction(action: TransactionAction, quantity: String, price: String, date: Long = 1L) =
        AssetTransaction(
            assetId = "crypto:BTC",
            assetType = AssetType.CRYPTO,
            symbol = "BTC",
            displayName = "Bitcoin",
            quoteCurrencyCode = "USD",
            action = action,
            dateInMillis = date,
            unitPrice = BigDecimal(price),
            quantity = BigDecimal(quantity)
        )

    private fun event(transaction: AssetTransaction) = AssetEvent(
        assetId = transaction.assetId,
        assetType = transaction.assetType,
        transaction = transaction,
        dateInMillis = transaction.dateInMillis
    )
}
