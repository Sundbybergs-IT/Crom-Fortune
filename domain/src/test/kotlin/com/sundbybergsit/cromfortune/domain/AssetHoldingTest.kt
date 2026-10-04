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
    fun `user scenario - buy, sell for profit, buy again - calculates correct GAV and realized profit`() {
        val holding = stockHolding()
        holding.aggregate(event(stockTransaction(TransactionAction.BUY, "10", "5")))
        holding.aggregate(event(stockTransaction(TransactionAction.SELL, "10", "10", date = 2L)))
        holding.aggregate(event(stockTransaction(TransactionAction.BUY, "5", "7", date = 3L)))

        assertEquals(BigDecimal("5"), holding.quantity)
        assertEquals(0, BigDecimal("7").compareTo(holding.acquisitionValue()))
        assertEquals(0, BigDecimal("50").compareTo(holding.realizedProfit()))
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

    private fun stockHolding() = AssetHolding(
        assetId = "stock:ERIC-B",
        assetType = AssetType.STOCK,
        symbol = "ERIC-B",
        displayName = "Ericsson",
        quoteCurrency = Currency.getInstance("SEK"),
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

    private fun stockTransaction(action: TransactionAction, quantity: String, price: String, date: Long = 1L) =
        AssetTransaction(
            assetId = "stock:ERIC-B",
            assetType = AssetType.STOCK,
            symbol = "ERIC-B",
            displayName = "Ericsson",
            quoteCurrencyCode = "SEK",
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

    @Test
    fun `stock dividend calculates correct dividends and profit without changing quantity or GAV`() {
        val holding = stockHolding()
        holding.aggregate(event(stockTransaction(TransactionAction.BUY, "10", "50", date = 1L)))
        holding.aggregate(event(stockTransaction(TransactionAction.DIVIDEND, "10", "2.50", date = 2L)))

        assertEquals(BigDecimal("10"), holding.quantity)
        assertEquals(BigDecimal("10"), holding.quantityAt(1L))
        assertEquals(BigDecimal("10"), holding.quantityAt(2L))
        assertEquals(0, BigDecimal("50").compareTo(holding.acquisitionValue()))
        assertEquals(0, BigDecimal("25.00").compareTo(holding.totalDividends))
        assertEquals(0, BigDecimal("25.00").compareTo(holding.profit(BigDecimal("50"))))
    }

    @Test
    fun `quantityAtExDate excludes shares purchased on or after Ex-date`() {
        val holding = stockHolding()
        holding.aggregate(event(stockTransaction(TransactionAction.BUY, "10", "50", date = 1L)))
        holding.aggregate(event(stockTransaction(TransactionAction.BUY, "5", "50", date = 2L)))

        assertEquals(BigDecimal("10"), holding.quantityAtExDate(2L))
        assertEquals(BigDecimal("15"), holding.quantityAt(2L))
    }
}
