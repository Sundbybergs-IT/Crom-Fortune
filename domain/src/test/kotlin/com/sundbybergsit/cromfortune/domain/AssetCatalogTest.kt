package com.sundbybergsit.cromfortune.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AssetCatalogTest {

    @Test
    fun `stock catalog preserves legacy stock definitions`() {
        val catalogStocks = AssetCatalog.stocks.map { asset ->
            Triple(asset.symbol, asset.displayName, asset.quoteCurrency.currencyCode)
        }

        assertEquals(StockPrice.SYMBOLS.toList(), catalogStocks)
    }

    @Test
    fun `stock can be found by stable ID and symbol`() {
        val legacyStock = StockPrice.SYMBOLS.first()
        val expectedAsset = AssetCatalog.findById("stock:${legacyStock.first}")

        assertEquals(expectedAsset, AssetCatalog.findBySymbol(AssetType.STOCK, legacyStock.first))
        assertEquals(legacyStock.second, expectedAsset?.displayName)
        assertEquals(legacyStock.third, expectedAsset?.quoteCurrency?.currencyCode)
    }

    @Test
    fun `initial cryptocurrencies have stable identity and Yahoo market symbols`() {
        assertEquals(
            listOf("crypto:BTC", "crypto:ETH"),
            AssetCatalog.cryptocurrencies.map(TradableAsset::id)
        )
        assertEquals(
            listOf("BTC-USD", "ETH-USD"),
            AssetCatalog.cryptocurrencies.map(TradableAsset::marketDataSymbol)
        )
        assertEquals(
            setOf(AssetType.CRYPTO),
            AssetCatalog.cryptocurrencies.map(TradableAsset::type).toSet()
        )
    }

    @Test
    fun `unsupported asset lookup returns null`() {
        assertNull(AssetCatalog.findById("crypto:UNKNOWN"))
        assertNull(AssetCatalog.findBySymbol(AssetType.CRYPTO, "UNKNOWN"))
    }
}
