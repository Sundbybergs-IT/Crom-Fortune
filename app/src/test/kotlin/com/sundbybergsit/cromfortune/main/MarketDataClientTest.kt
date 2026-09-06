package com.sundbybergsit.cromfortune.main

import com.sundbybergsit.cromfortune.domain.AssetCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MarketDataClientTest {

    @Test
    fun `provider symbols map mixed prices to stable asset IDs`() {
        val stock = AssetCatalog.stocks.first()
        val crypto = AssetCatalog.cryptocurrencies.first()

        val result = mapMarketDataPrices(
            assets = listOf(stock, crypto),
            pricesByMarketDataSymbol = mapOf(
                stock.marketDataSymbol to 125.5,
                crypto.marketDataSymbol to 62_500.25
            )
        )

        assertEquals(setOf(stock.id, crypto.id), result.keys)
        assertEquals(stock.quoteCurrency, result.getValue(stock.id).currency)
        assertEquals(0, 62_500.25.toBigDecimal().compareTo(result.getValue(crypto.id).price))
    }

    @Test
    fun `missing provider quote is omitted`() {
        val stock = AssetCatalog.stocks.first()
        val crypto = AssetCatalog.cryptocurrencies.first()

        val result = mapMarketDataPrices(
            assets = listOf(stock, crypto),
            pricesByMarketDataSymbol = mapOf(stock.marketDataSymbol to 125.5)
        )

        assertEquals(setOf(stock.id), result.keys)
        assertFalse(result.containsKey(crypto.id))
    }
}
