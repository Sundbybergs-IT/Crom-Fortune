package com.sundbybergsit.cromfortune.main.ui.home

import com.sundbybergsit.cromfortune.domain.AssetEvent
import com.sundbybergsit.cromfortune.domain.AssetHolding
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.StockEvent
import com.sundbybergsit.cromfortune.domain.StockOrderAggregate
import java.math.BigDecimal
import java.util.Currency

data class PortfolioItem(
    val assetId: String,
    val assetType: AssetType,
    val symbol: String,
    val displayName: String,
    val currency: Currency,
    val quantity: BigDecimal,
    val acquisitionValue: BigDecimal,
    val assetEvents: List<AssetEvent> = emptyList(),
    val legacyStockEvents: List<StockEvent> = emptyList(),
    private val profitCalculator: (BigDecimal) -> BigDecimal
) {
    fun profit(currentPrice: BigDecimal): BigDecimal = profitCalculator(currentPrice)

    companion object {
        fun fromAssetHolding(holding: AssetHolding) = PortfolioItem(
            assetId = holding.assetId,
            assetType = holding.assetType,
            symbol = holding.symbol,
            displayName = (if (holding.assetType == AssetType.CRYPTO) "[CRYPTO] " else "") +
                "${holding.displayName} (${holding.symbol})",
            currency = holding.quoteCurrency,
            quantity = holding.quantity,
            acquisitionValue = holding.acquisitionValue(),
            assetEvents = holding.events,
            profitCalculator = holding::profit
        )

        fun fromStockCompatibility(aggregate: StockOrderAggregate) = PortfolioItem(
            assetId = "stock:${aggregate.stockSymbol}",
            assetType = AssetType.STOCK,
            symbol = aggregate.stockSymbol,
            displayName = aggregate.displayName,
            currency = aggregate.currency,
            quantity = aggregate.getExactQuantity(),
            acquisitionValue = aggregate.getAcquisitionValue().toBigDecimal(),
            legacyStockEvents = aggregate.events,
            profitCalculator = { price -> aggregate.getProfit(price.toDouble()).toBigDecimal() }
        )
    }
}
