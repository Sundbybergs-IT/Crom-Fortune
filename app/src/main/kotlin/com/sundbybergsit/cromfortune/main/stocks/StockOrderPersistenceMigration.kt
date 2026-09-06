package com.sundbybergsit.cromfortune.main.stocks

import android.content.Context
import android.util.Log
import com.sundbybergsit.cromfortune.domain.StockOrder
import kotlinx.serialization.json.Json

object StockOrderPersistenceMigration {

    private const val TAG = "StockOrderMigration"
    private const val MIGRATION_PREFERENCES = "DataMigrations"
    private const val ORDER_SCHEMA_VERSION = 2
    private const val STABLE_KEY_VERSION = 3

    fun migrateToLatest(context: Context, portfolioNames: Iterable<String>) {
        migrateToVersion2(context, portfolioNames)
        val migrationPreferences = context.getSharedPreferences(MIGRATION_PREFERENCES, Context.MODE_PRIVATE)
        for (portfolioName in portfolioNames) {
            val migrationKey = "asset-transactions:$portfolioName"
            if (migrationPreferences.getInt(migrationKey, 0) >= STABLE_KEY_VERSION) continue

            try {
                migrateStableKeys(context, portfolioName)
                check(migrationPreferences.edit().putInt(migrationKey, STABLE_KEY_VERSION).commit()) {
                    "Failed to record stable-key migration for portfolio $portfolioName"
                }
            } catch (error: Exception) {
                Log.e(TAG, "Keeping the original data after migration failed for portfolio $portfolioName", error)
            }
        }
    }

    fun migrateToVersion2(context: Context, portfolioNames: Iterable<String>) {
        val migrationPreferences = context.getSharedPreferences(MIGRATION_PREFERENCES, Context.MODE_PRIVATE)
        for (portfolioName in portfolioNames) {
            val migrationKey = "stock-orders:$portfolioName"
            if (migrationPreferences.getInt(migrationKey, 0) >= ORDER_SCHEMA_VERSION) continue
            try {
                migrateOrderSchema(context, portfolioName)
                check(migrationPreferences.edit().putInt(migrationKey, ORDER_SCHEMA_VERSION).commit()) {
                    "Failed to record stock-order migration for portfolio $portfolioName"
                }
            } catch (error: Exception) {
                Log.e(TAG, "Keeping the original data after migration failed for portfolio $portfolioName", error)
            }
        }
    }

    private fun migrateOrderSchema(context: Context, portfolioName: String) {
        val source = context.getSharedPreferences(portfolioName, Context.MODE_PRIVATE)
        if (source.all.isEmpty()) return
        val originalValues = readValues(source.all, portfolioName)
        val migratedValues = originalValues.mapValues { (_, serializedSets) ->
            serializedSets.map { serializedSet ->
                Json.encodeToString(Json.decodeFromString<Set<StockOrder>>(serializedSet))
            }.toSet()
        }
        backup(context, "$portfolioName-v1-backup", originalValues, portfolioName)
        writeReplacingAll(source, migratedValues, portfolioName)
    }

    private fun migrateStableKeys(context: Context, portfolioName: String) {
        val source = context.getSharedPreferences(portfolioName, Context.MODE_PRIVATE)
        if (source.all.isEmpty()) return

        val originalValues = readValues(source.all, portfolioName)
        val migratedValues = linkedMapOf<String, Set<String>>()
        originalValues.forEach { (legacyKey, serializedSets) ->
            val orders = serializedSets.flatMap { serializedSet ->
                val orders: Set<StockOrder> = Json.decodeFromString(serializedSet)
                orders
            }
            val stableIds = orders.map(StockOrder::assetId).toSet()
            require(stableIds.size <= 1) { "Legacy key $legacyKey contains multiple asset IDs: $stableIds" }
            val stableKey = stableIds.singleOrNull()
                ?: if (legacyKey.contains(':')) legacyKey else "stock:$legacyKey"
            val existing = migratedValues[stableKey]
            require(existing == null || existing == serializedSets) {
                "Conflicting legacy and stable values for $stableKey"
            }
            migratedValues[stableKey] = serializedSets
        }

        backup(context, "$portfolioName-v2-backup", originalValues, portfolioName)
        writeReplacingAll(source, migratedValues, portfolioName)
    }

    private fun readValues(values: Map<String, *>, portfolioName: String): Map<String, Set<String>> =
        values.mapValues { (key, value) ->
            require(value is Set<*>) { "Unexpected value for portfolio $portfolioName and asset $key" }
            value.map { entry -> requireNotNull(entry as? String) }.toSet()
        }

    private fun backup(
        context: Context,
        backupName: String,
        originalValues: Map<String, Set<String>>,
        portfolioName: String
    ) {
        val backup = context.getSharedPreferences(backupName, Context.MODE_PRIVATE)
        if (backup.all.isEmpty()) {
            val backupEditor = backup.edit()
            originalValues.forEach { (key, value) -> backupEditor.putStringSet(key, value) }
            check(backupEditor.commit()) { "Failed to back up portfolio $portfolioName" }
        }
    }

    private fun writeReplacingAll(
        source: android.content.SharedPreferences,
        values: Map<String, Set<String>>,
        portfolioName: String
    ) {
        val sourceEditor = source.edit().clear()
        values.forEach { (key, value) -> sourceEditor.putStringSet(key, value) }
        check(sourceEditor.commit()) { "Failed to migrate portfolio $portfolioName" }
    }
}
