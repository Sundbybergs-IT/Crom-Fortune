package com.sundbybergsit.cromfortune.main

import com.sundbybergsit.cromfortune.domain.AssetCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Test
import java.util.Currency

@Ignore("Opt-in live Yahoo contract test; excluded from normal CI because it requires the provider")
class YahooMarketDataContractTest {

    @Test
    fun `configured crypto symbols return positive USD quotes`() {
        val cryptocurrencies = AssetCatalog.cryptocurrencies

        assertEquals(listOf("BTC-USD", "ETH-USD"), cryptocurrencies.map { it.marketDataSymbol })
        assertTrue(cryptocurrencies.all { it.quoteCurrency == Currency.getInstance("USD") })

        val result = YahooMarketDataClient.getPrices(cryptocurrencies)

        assertTrue(result.failures.toString(), result.failures.isEmpty())
        assertEquals(cryptocurrencies.map { it.id }.toSet(), result.prices.keys)
        assertTrue(result.prices.values.all { price -> price.price.isFinite() && price.price > 0.0 })
    }
}
