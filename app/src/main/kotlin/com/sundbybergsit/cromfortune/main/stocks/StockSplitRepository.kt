package com.sundbybergsit.cromfortune.main.stocks

import android.content.Context
import android.content.SharedPreferences
import com.sundbybergsit.cromfortune.domain.StockSplit
import com.sundbybergsit.cromfortune.domain.StockSplitApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// FIXME: Convert to datastore, https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
class StockSplitRepository(
    context: Context,
    porfolioName: String,
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("$porfolioName-splits", Context.MODE_PRIVATE),
) : StockSplitApi {

    override fun remove(stockSplit: StockSplit) {
        val stockSplits = list(stockSplit.name).toMutableSet()
        stockSplits.remove(stockSplit)
        if (stockSplits.isEmpty()) {
            remove(stockSplit.name)
        } else {
            sharedPreferences.edit().putString(stockSplit.name, Json.encodeToString(stockSplits)).apply()
        }
    }

    override fun remove(stockName: String) {
        sharedPreferences.edit().remove(stockName).apply()
    }

    override fun list(stockName: String): Set<StockSplit> {
        val serializedStockSplits = sharedPreferences.getString(stockName, null)
        val result = mutableSetOf<StockSplit>()
        return if (serializedStockSplits != null) {
            val setOfStockSplits: Set<StockSplit> = Json.decodeFromString(serializedStockSplits)
            for (serializedStockSplit in serializedStockSplits) {
                result.addAll(setOfStockSplits)
            }
            result
        } else {
            setOf()
        }
    }

    override fun putAll(stockName: String, stockSplits: Set<StockSplit>) {
        sharedPreferences.edit().putString(stockName, Json.encodeToString(stockSplits)).apply()
    }

    override fun putReplacingAll(stockName: String, stockSplit: StockSplit) {
        putAll(stockName, setOf(stockSplit))
    }

}
