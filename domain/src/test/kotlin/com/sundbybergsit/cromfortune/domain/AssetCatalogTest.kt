package com.sundbybergsit.cromfortune.domain

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class AssetCatalogTest {

    @Test
    fun `stock catalog contains expected definitions`() {
        assertNotNull(AssetCatalog.stocks)
        assertEquals(true, AssetCatalog.stocks.isNotEmpty())
    }

    @Test
    fun `stock can be found by stable ID and symbol`() {
        val legacyStock = AssetCatalog.stocks.first()
        val expectedAsset = AssetCatalog.findById("stock:${legacyStock.symbol}")

        assertEquals(expectedAsset, AssetCatalog.findBySymbol(AssetType.STOCK, legacyStock.symbol))
        assertEquals(legacyStock.displayName, expectedAsset?.displayName)
        assertEquals(legacyStock.quoteCurrency.currencyCode, expectedAsset?.quoteCurrency?.currencyCode)
    }

    @Test
    fun `delisted stocks remain addressable but are excluded from active assets`() {
        val delistedStock = AssetCatalog.findBySymbol(AssetType.STOCK, "SAS.ST")

        assertEquals(AssetStatus.DELISTED, delistedStock?.status)
        assertEquals(delistedStock, AssetCatalog.findById("stock:SAS.ST"))
        assertEquals(false, AssetCatalog.activeAssets.contains(delistedStock))
        assertEquals(false, AssetCatalog.activeStocks.contains(delistedStock))
    }

    @Test
    fun `initial cryptocurrencies have stable identity and Yahoo market symbols`() {
        assertEquals(
            listOf("crypto:BTC", "crypto:ETH", "crypto:LTC"),
            AssetCatalog.cryptocurrencies.map(TradableAsset::id)
        )
        assertEquals(
            listOf("BTC-USD", "ETH-USD", "LTC-USD"),
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
