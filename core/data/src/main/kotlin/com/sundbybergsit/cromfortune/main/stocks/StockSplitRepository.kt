package com.sundbybergsit.cromfortune.main.stocks

import android.content.Context
import com.sundbybergsit.cromfortune.domain.StockSplit
import com.sundbybergsit.cromfortune.domain.StockSplitApi
import com.sundbybergsit.cromfortune.main.db.CromFortuneDatabase
import com.sundbybergsit.cromfortune.main.db.StockSplitDao
import com.sundbybergsit.cromfortune.main.db.StockSplitEntity

class StockSplitRepository(
    context: Context,
    private val porfolioName: String,
    private val dao: StockSplitDao = CromFortuneDatabase.getInstance(context).stockSplitDao()
) : StockSplitApi {

    fun update(original: StockSplit, updated: StockSplit) {
        val originalSplits = list(original.name)
        require(original in originalSplits) { "Stock split to update no longer exists" }

        val remainingOriginals = originalSplits - original
        if (original.name == updated.name) {
            putAll(original.name, remainingOriginals + updated)
        } else {
            if (remainingOriginals.isEmpty()) {
                remove(original.name)
            } else {
                putAll(original.name, remainingOriginals)
            }
            putAll(updated.name, list(updated.name) + updated)
        }
    }

    override fun remove(stockSplit: StockSplit) {
        val stockSplits = list(stockSplit.name).toMutableSet()
        stockSplits.remove(stockSplit)
        if (stockSplits.isEmpty()) {
            remove(stockSplit.name)
        } else {
            putAll(stockSplit.name, stockSplits)
        }
    }

    override fun remove(stockName: String) {
        dao.deleteForStock(porfolioName, stockName)
    }

    override fun list(stockName: String): Set<StockSplit> {
        return dao.getSplitsForStock(porfolioName, stockName).map { it.toDomain() }.toSet()
    }

    override fun putAll(stockName: String, stockSplits: Set<StockSplit>) {
        dao.deleteForStock(porfolioName, stockName)
        val entities = stockSplits.map { StockSplitEntity.fromDomain(porfolioName, it) }
        dao.insertAll(entities)
    }

    override fun putReplacingAll(stockName: String, stockSplit: StockSplit) {
        putAll(stockName, setOf(stockSplit))
    }

}
