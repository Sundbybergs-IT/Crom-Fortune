package com.sundbybergsit.cromfortune.domain

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class StockOrderSerializationTest {

    @Test
    fun `legacy numeric quantity decodes as exact decimal`() {
        val legacyJson =
            """{"orderAction":"Buy","currency":"USD","dateInMillis":1,"name":"BTC","pricePerStock":100.0,"quantity":3}"""

        val order = Json.decodeFromString<StockOrder>(legacyJson)

        assertEquals(BigDecimal("3"), order.quantity)
        assertEquals("stock:BTC", order.assetId)
        assertEquals(AssetType.STOCK, order.assetType)
    }

    @Test
    fun `fractional quantity serializes as a decimal string and round trips exactly`() {
        val order = StockOrder(
            orderAction = "Buy",
            currency = "USD",
            dateInMillis = 1,
            name = "BTC",
            pricePerStock = 62_500.0,
            quantity = BigDecimal("0.00000001"),
            assetId = "crypto:BTC",
            assetType = AssetType.CRYPTO
        )

        val encoded = Json.encodeToString(order)
        val decoded = Json.decodeFromString<StockOrder>(encoded)

        assertTrue(encoded.contains("\"quantity\":\"0.00000001\""))
        assertTrue(encoded.contains("\"schemaVersion\":2"))
        assertEquals(order, decoded)
    }
}
