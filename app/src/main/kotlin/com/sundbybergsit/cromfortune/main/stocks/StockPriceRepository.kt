package com.sundbybergsit.cromfortune.main.stocks

import android.util.Log
import androidx.annotation.VisibleForTesting
import com.sundbybergsit.cromfortune.domain.AssetCatalog
import com.sundbybergsit.cromfortune.domain.AssetPrice
import com.sundbybergsit.cromfortune.domain.AssetPriceApi
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.StockPrice
import com.sundbybergsit.cromfortune.domain.StockPriceApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

object StockPriceRepository : StockPriceApi, AssetPriceApi {

    private const val TAG = "StockPriceRepository"

    private val _stockPricesStateFlow: MutableStateFlow<ViewState> = MutableStateFlow(getPristineViewState())
    private val _assetPricesStateFlow: MutableStateFlow<AssetViewState> = MutableStateFlow(getPristineAssetViewState())

    val stockPricesStateFlow: StateFlow<ViewState> = _stockPricesStateFlow.asStateFlow()
    val assetPricesStateFlow: StateFlow<AssetViewState> = _assetPricesStateFlow.asStateFlow()

    override fun put(stockPrice: Set<StockPrice>) {
        Log.v(TAG, "put(${stockPrice})")
        val retainedNonStockPrices = assetPricesStateFlow.value.assetPrices.filterNot { assetPrice ->
            AssetCatalog.findById(assetPrice.assetId)?.type == AssetType.STOCK || assetPrice.assetId.startsWith("stock:")
        }
        val convertedStockPrices = stockPrice.map { legacyPrice ->
            val assetId = AssetCatalog.findBySymbol(AssetType.STOCK, legacyPrice.stockSymbol)?.id
                ?: "stock:${legacyPrice.stockSymbol}"
            AssetPrice(assetId, legacyPrice.currency, legacyPrice.price)
        }
        putAssetPrices((retainedNonStockPrices + convertedStockPrices).toSet())
    }

    override fun getStockPrice(stockSymbol: String): StockPrice? =
        stockPricesStateFlow.value.stockPrices.find { stockPrice -> stockPrice.stockSymbol == stockSymbol }

    override fun putAssetPrices(assetPrices: Set<AssetPrice>) {
        Log.v(TAG, "putAssetPrices(${assetPrices})")
        val instant = Instant.now()
        publish(
            instant,
            assetPrices.associate { assetPrice ->
                assetPrice.assetId to AssetPriceStatus(assetPrice, instant, isStale = false)
            }
        )
    }

    fun updateAssetPrices(
        assetPrices: Collection<AssetPrice>,
        requestedAssetIds: Set<String>,
        retrievedAt: Instant = Instant.now()
    ) {
        val statuses = assetPricesStateFlow.value.statuses.associateBy { status -> status.assetPrice.assetId }.toMutableMap()
        val newPricesById = assetPrices.associateBy(AssetPrice::assetId)
        for (assetId in requestedAssetIds) {
            val newPrice = newPricesById[assetId]
            if (newPrice != null) {
                statuses[assetId] = AssetPriceStatus(newPrice, retrievedAt, isStale = false)
            } else {
                statuses[assetId] = statuses[assetId]?.copy(isStale = true) ?: continue
            }
        }
        publish(retrievedAt, statuses)
    }

    private fun publish(instant: Instant, statusesById: Map<String, AssetPriceStatus>) {
        val statuses = statusesById.values.toSet()
        val assetPrices = statuses.map(AssetPriceStatus::assetPrice).toSet()
        _assetPricesStateFlow.value = AssetViewState(instant, assetPrices, statuses)
        _stockPricesStateFlow.value = ViewState(
            stockPrices = assetPrices.mapNotNull { assetPrice ->
                val asset = AssetCatalog.findById(assetPrice.assetId)
                if (asset?.type == AssetType.STOCK) {
                    StockPrice(asset.symbol, assetPrice.currency, assetPrice.price.toDouble())
                } else {
                    null
                }
            }.toSet()
        )
    }

    override fun getAssetPrice(assetId: String): AssetPrice? =
        assetPricesStateFlow.value.assetPrices.find { assetPrice -> assetPrice.assetId == assetId }

    @VisibleForTesting
    fun clear() {
        _stockPricesStateFlow.value = getPristineViewState()
        _assetPricesStateFlow.value = getPristineAssetViewState()
    }

    private fun getPristineViewState() = ViewState(setOf())
    private fun getPristineAssetViewState() = AssetViewState(Instant.now(), setOf(), setOf())

    class ViewState(val stockPrices: Set<StockPrice>)
    data class AssetPriceStatus(val assetPrice: AssetPrice, val lastUpdatedAt: Instant, val isStale: Boolean)
    class AssetViewState(
        val instant: Instant,
        val assetPrices: Set<AssetPrice>,
        val statuses: Set<AssetPriceStatus>
    )

}
