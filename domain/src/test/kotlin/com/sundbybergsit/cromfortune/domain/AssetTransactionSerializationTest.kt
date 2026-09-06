package com.sundbybergsit.cromfortune.domain

import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class AssetTransactionSerializationTest {

    @Test
    fun `unknown asset metadata and decimal values round-trip`() {
        val transaction = AssetTransaction(
            assetId = "crypto:retired",
            assetType = AssetType.CRYPTO,
            symbol = "OLD",
            displayName = "Retired Coin",
            quoteCurrencyCode = "EUR",
            action = TransactionAction.BUY,
            dateInMillis = 1L,
            unitPrice = BigDecimal("0.000000123456789"),
            commissionFee = BigDecimal("0.01"),
            quantity = BigDecimal("0.00000001")
        )

        val json = Json.encodeToString(transaction)
        val decoded = Json.decodeFromString<AssetTransaction>(json)

        assertEquals(transaction, decoded)
        assertTrue(json.contains("\"unitPrice\":\"0.000000123456789\""))
        assertTrue(json.contains("\"displayName\":\"Retired Coin\""))
    }
}
