package com.sundbybergsit.cromfortune.main.stocks

import android.content.Context
import android.util.Log
import com.sundbybergsit.cromfortune.domain.StockOrder
import kotlinx.serialization.json.Json

object StockOrderPersistenceMigration {

    private const val TAG = "StockOrderMigration"
    private const val MIGRATION_PREFERENCES = "DataMigrations"
    private const val MIGRATION_VERSION = 2

    fun migrateToVersion2(context: Context, portfolioNames: Iterable<String>) {
        val migrationPreferences = context.getSharedPreferences(MIGRATION_PREFERENCES, Context.MODE_PRIVATE)
        for (portfolioName in portfolioNames) {
            val migrationKey = "stock-orders:$portfolioName"
            if (migrationPreferences.getInt(migrationKey, 0) >= MIGRATION_VERSION) continue

            try {
                migratePortfolio(context, portfolioName)
                check(migrationPreferences.edit().putInt(migrationKey, MIGRATION_VERSION).commit()) {
                    "Failed to record stock-order migration for portfolio $portfolioName"
                }
            } catch (error: Exception) {
                Log.e(TAG, "Keeping the original data after migration failed for portfolio $portfolioName", error)
            }
        }
    }

    private fun migratePortfolio(context: Context, portfolioName: String) {
        val source = context.getSharedPreferences(portfolioName, Context.MODE_PRIVATE)
        if (source.all.isEmpty()) return

        val originalValues = source.all.mapValues { (key, value) ->
            require(value is Set<*>) { "Unexpected value for portfolio $portfolioName and asset $key" }
            value.map { entry -> requireNotNull(entry as? String) }.toSet()
        }
        val migratedValues = originalValues.mapValues { (_, serializedSets) ->
            serializedSets.map { serializedSet ->
                val orders: Set<StockOrder> = Json.decodeFromString(serializedSet)
                Json.encodeToString(orders)
            }.toSet()
        }

        val backup = context.getSharedPreferences("$portfolioName-v1-backup", Context.MODE_PRIVATE)
        if (backup.all.isEmpty()) {
            val backupEditor = backup.edit()
            originalValues.forEach { (key, value) -> backupEditor.putStringSet(key, value) }
            check(backupEditor.commit()) { "Failed to back up portfolio $portfolioName" }
        }

        val sourceEditor = source.edit()
        migratedValues.forEach { (key, value) -> sourceEditor.putStringSet(key, value) }
        check(sourceEditor.commit()) { "Failed to migrate portfolio $portfolioName" }
    }
}
