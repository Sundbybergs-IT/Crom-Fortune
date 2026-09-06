package com.sundbybergsit.cromfortune.main.stocks

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.domain.AssetCatalog
import com.sundbybergsit.cromfortune.domain.AssetPrice
import com.sundbybergsit.cromfortune.main.CromTestRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class StockPriceRepositoryTest {

    @get:Rule
    val cromTestRule = CromTestRule()

    @Test
    fun `partial refresh updates successes and retains missing prices as stale`() {
        val stock = AssetCatalog.stocks.first()
        val crypto = AssetCatalog.cryptocurrencies.first()
        val initialTime = Instant.parse("2026-01-01T00:00:00Z")
        val refreshTime = Instant.parse("2026-01-01T00:05:00Z")
        val initialStockPrice = AssetPrice(stock.id, stock.quoteCurrency, 100.0)
        val initialCryptoPrice = AssetPrice(crypto.id, crypto.quoteCurrency, 60_000.0)

        StockPriceRepository.updateAssetPrices(
            assetPrices = listOf(initialStockPrice, initialCryptoPrice),
            requestedAssetIds = setOf(stock.id, crypto.id),
            retrievedAt = initialTime
        )
        StockPriceRepository.updateAssetPrices(
            assetPrices = listOf(AssetPrice(stock.id, stock.quoteCurrency, 101.0)),
            requestedAssetIds = setOf(stock.id, crypto.id),
            retrievedAt = refreshTime
        )

        val statuses = StockPriceRepository.assetPricesStateFlow.value.statuses
            .associateBy { status -> status.assetPrice.assetId }
        assertEquals(101.0, statuses.getValue(stock.id).assetPrice.price, 0.0)
        assertEquals(refreshTime, statuses.getValue(stock.id).lastUpdatedAt)
        assertFalse(statuses.getValue(stock.id).isStale)
        assertEquals(initialCryptoPrice, statuses.getValue(crypto.id).assetPrice)
        assertEquals(initialTime, statuses.getValue(crypto.id).lastUpdatedAt)
        assertTrue(statuses.getValue(crypto.id).isStale)
    }
}
