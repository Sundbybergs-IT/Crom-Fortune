package com.sundbybergsit.cromfortune.main.stocks

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.domain.StockOrder
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class StockOrderPersistenceMigrationTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `migration backs up legacy JSON and writes version 2 orders`() {
        val portfolioName = "migration-test-${System.nanoTime()}"
        val legacyJson =
            """[{"orderAction":"Buy","currency":"USD","dateInMillis":1,"name":"MSFT","pricePerStock":100.0,"quantity":3}]"""
        val source = context.getSharedPreferences(portfolioName, Context.MODE_PRIVATE)
        source.edit().putStringSet("MSFT", setOf(legacyJson)).commit()

        StockOrderPersistenceMigration.migrateToVersion2(context, listOf(portfolioName))

        val migratedJson = source.getStringSet("MSFT", emptySet()).orEmpty().single()
        val migratedOrders = Json.decodeFromString<Set<StockOrder>>(migratedJson)
        val backupJson = context.getSharedPreferences("$portfolioName-v1-backup", Context.MODE_PRIVATE)
            .getStringSet("MSFT", emptySet()).orEmpty().single()

        assertEquals(legacyJson, backupJson)
        assertEquals(BigDecimal("3"), migratedOrders.single().quantity)
        assertEquals("stock:MSFT", migratedOrders.single().assetId)
        assertTrue(migratedJson.contains("\"quantity\":\"3\""))
        assertTrue(migratedJson.contains("\"schemaVersion\":2"))
    }

    @Test
    fun `malformed legacy JSON remains untouched`() {
        val portfolioName = "malformed-migration-test-${System.nanoTime()}"
        val malformedJson = "not-json"
        val source = context.getSharedPreferences(portfolioName, Context.MODE_PRIVATE)
        source.edit().putStringSet("MSFT", setOf(malformedJson)).commit()

        StockOrderPersistenceMigration.migrateToVersion2(context, listOf(portfolioName))

        assertEquals(setOf(malformedJson), source.getStringSet("MSFT", emptySet()))
        assertTrue(
            context.getSharedPreferences("$portfolioName-v1-backup", Context.MODE_PRIVATE).all.isEmpty()
        )
    }
}
