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

    @Test
    fun `latest migration moves legacy symbol key to stable asset ID and is idempotent`() {
        val portfolioName = "stable-key-migration-${System.nanoTime()}"
        val legacyJson =
            """[{"orderAction":"Buy","currency":"USD","dateInMillis":1,"name":"MSFT","pricePerStock":100.0,"quantity":3}]"""
        val source = context.getSharedPreferences(portfolioName, Context.MODE_PRIVATE)
        source.edit().putStringSet("MSFT", setOf(legacyJson)).commit()

        StockOrderPersistenceMigration.migrateToLatest(context, listOf(portfolioName))
        val firstResult = source.all
        StockOrderPersistenceMigration.migrateToLatest(context, listOf(portfolioName))

        assertEquals(setOf("stock:MSFT"), source.all.keys)
        assertEquals(firstResult, source.all)
        assertTrue(context.getSharedPreferences("$portfolioName-v2-backup", Context.MODE_PRIVATE)
            .contains("MSFT"))
    }

    @Test
    fun `stable-key conflict leaves source untouched`() {
        val portfolioName = "stable-key-conflict-${System.nanoTime()}"
        val first = setOf(Json.encodeToString(setOf(StockOrder("Buy", "USD", 1L, "MSFT", 100.0, quantity = 1))))
        val second = setOf(Json.encodeToString(setOf(StockOrder("Buy", "USD", 2L, "MSFT", 101.0, quantity = 1))))
        val source = context.getSharedPreferences(portfolioName, Context.MODE_PRIVATE)
        source.edit().putStringSet("MSFT", first).putStringSet("stock:MSFT", second).commit()

        StockOrderPersistenceMigration.migrateToLatest(context, listOf(portfolioName))

        assertEquals(setOf("MSFT", "stock:MSFT"), source.all.keys)
    }
}
