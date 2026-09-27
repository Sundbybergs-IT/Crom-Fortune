package com.sundbybergsit.cromfortune.main.stocks

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.sundbybergsit.cromfortune.domain.StockOrder
import kotlinx.serialization.json.Json

object StockOrderPersistenceMigration {

    private const val TAG = "StockOrderMigration"
    private const val MIGRATION_PREFERENCES = "DataMigrations"
    private const val ORDER_SCHEMA_VERSION = 2
    private const val STABLE_KEY_VERSION = 3

    fun migrateToLatest(context: Context, portfolioNames: Iterable<String>) {
        migrateToLatest(portfolioNames) { name ->
            SharedPreferencesMigrationStore(context.getSharedPreferences(name, Context.MODE_PRIVATE))
        }
    }

    internal fun migrateToLatest(
        portfolioNames: Iterable<String>,
        store: (String) -> MigrationStore
    ) {
        migrateToVersion2(portfolioNames, store)
        val migrationPreferences = store(MIGRATION_PREFERENCES)
        for (portfolioName in portfolioNames) {
            if (migrationPreferences.getInt("stock-orders:$portfolioName", 0) < ORDER_SCHEMA_VERSION) {
                continue
            }
            val migrationKey = "asset-transactions:$portfolioName"
            if (migrationPreferences.getInt(migrationKey, 0) >= STABLE_KEY_VERSION) continue

            try {
                migrateStableKeys(portfolioName, store)
                check(migrationPreferences.putInt(migrationKey, STABLE_KEY_VERSION)) {
                    "Failed to record stable-key migration for portfolio $portfolioName"
                }
            } catch (error: Exception) {
                Log.e(TAG, "Keeping the original data after migration failed for portfolio $portfolioName", error)
            }
        }
    }

    fun migrateToVersion2(context: Context, portfolioNames: Iterable<String>) {
        migrateToVersion2(portfolioNames) { name ->
            SharedPreferencesMigrationStore(context.getSharedPreferences(name, Context.MODE_PRIVATE))
        }
    }

    internal fun migrateToVersion2(
        portfolioNames: Iterable<String>,
        store: (String) -> MigrationStore
    ) {
        val migrationPreferences = store(MIGRATION_PREFERENCES)
        for (portfolioName in portfolioNames) {
            val migrationKey = "stock-orders:$portfolioName"
            if (migrationPreferences.getInt(migrationKey, 0) >= ORDER_SCHEMA_VERSION) continue
            try {
                migrateOrderSchema(portfolioName, store)
                check(migrationPreferences.putInt(migrationKey, ORDER_SCHEMA_VERSION)) {
                    "Failed to record stock-order migration for portfolio $portfolioName"
                }
            } catch (error: Exception) {
                Log.e(TAG, "Keeping the original data after migration failed for portfolio $portfolioName", error)
            }
        }
    }

    private fun migrateOrderSchema(portfolioName: String, store: (String) -> MigrationStore) {
        val source = store(portfolioName)
        if (source.all.isEmpty()) return
        val originalValues = readValues(source.all, portfolioName)
        val migratedValues = originalValues.mapValues { (_, serializedSets) ->
            serializedSets.map { serializedSet ->
                Json.encodeToString(Json.decodeFromString<Set<StockOrder>>(serializedSet))
            }.toSet()
        }
        backup(store("$portfolioName-v1-backup"), originalValues, portfolioName)
        writeReplacingAll(source, migratedValues, portfolioName)
    }

    private fun migrateStableKeys(portfolioName: String, store: (String) -> MigrationStore) {
        val source = store(portfolioName)
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

        backup(store("$portfolioName-v2-backup"), originalValues, portfolioName)
        writeReplacingAll(source, migratedValues, portfolioName)
    }

    private fun readValues(values: Map<String, *>, portfolioName: String): Map<String, Set<String>> =
        values.mapValues { (key, value) ->
            require(value is Set<*>) { "Unexpected value for portfolio $portfolioName and asset $key" }
            value.map { entry -> requireNotNull(entry as? String) }.toSet()
        }

    private fun backup(
        backup: MigrationStore,
        originalValues: Map<String, Set<String>>,
        portfolioName: String
    ) {
        if (backup.all.isEmpty()) {
            check(backup.replaceAll(originalValues)) { "Failed to back up portfolio $portfolioName" }
        }
    }

    private fun writeReplacingAll(
        source: MigrationStore,
        values: Map<String, Set<String>>,
        portfolioName: String
    ) {
        check(source.replaceAll(values)) { "Failed to migrate portfolio $portfolioName" }
    }
}

internal interface MigrationStore {
    val all: Map<String, *>
    fun getInt(key: String, defaultValue: Int): Int
    fun putInt(key: String, value: Int): Boolean
    fun replaceAll(values: Map<String, Set<String>>): Boolean
}

private class SharedPreferencesMigrationStore(
    private val preferences: SharedPreferences
) : MigrationStore {
    override val all: Map<String, *> get() = preferences.all

    override fun getInt(key: String, defaultValue: Int): Int = preferences.getInt(key, defaultValue)

    override fun putInt(key: String, value: Int): Boolean =
        preferences.edit().putInt(key, value).commit()

    override fun replaceAll(values: Map<String, Set<String>>): Boolean {
        val editor = preferences.edit().clear()
        values.forEach { (key, value) -> editor.putStringSet(key, value) }
        return editor.commit()
    }
}
