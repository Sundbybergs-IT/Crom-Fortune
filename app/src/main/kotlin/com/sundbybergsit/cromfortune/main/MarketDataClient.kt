package com.sundbybergsit.cromfortune.main

import com.sundbybergsit.cromfortune.domain.AssetPrice
import com.sundbybergsit.cromfortune.domain.TradableAsset
import yahoofinance.get
import yahoofinance.getFxHax
import java.math.BigDecimal
import java.util.Currency

interface MarketDataClient {

    fun getRateInSek(currency: Currency): Double

    fun getPrices(assets: Collection<TradableAsset>): MarketDataResult
}

data class MarketDataResult(
    val prices: Map<String, AssetPrice>,
    val failures: Map<String, String> = emptyMap()
)

object YahooMarketDataClient : MarketDataClient {

    override fun getRateInSek(currency: Currency): Double =
        getFxHax("${currency.currencyCode}SEK=X")?.price?.toDouble() ?: 1.0

    override fun getPrices(assets: Collection<TradableAsset>): MarketDataResult {
        return try {
            val marketDataPrices = get(assets.map(TradableAsset::marketDataSymbol).toTypedArray())
                .mapNotNull { (marketDataSymbol, stock) ->
                    stock.quote?.price?.let { price -> marketDataSymbol to price }
                }.toMap()
            val prices = mapExactMarketDataPrices(assets, marketDataPrices)
            MarketDataResult(
                prices = prices,
                failures = assets.filterNot { asset -> prices.containsKey(asset.id) }
                    .associate { asset -> asset.id to "Quote missing from Yahoo response" }
            )
        } catch (error: Exception) {
            MarketDataResult(
                prices = emptyMap(),
                failures = assets.associate { asset -> asset.id to (error.message ?: error.javaClass.simpleName) }
            )
        }
    }
}

internal fun mapMarketDataPrices(
    assets: Collection<TradableAsset>,
    pricesByMarketDataSymbol: Map<String, Double>
): Map<String, AssetPrice> = mapExactMarketDataPrices(
    assets,
    pricesByMarketDataSymbol.mapValues { (_, price) -> price.toBigDecimal() }
)

internal fun mapExactMarketDataPrices(
    assets: Collection<TradableAsset>,
    pricesByMarketDataSymbol: Map<String, BigDecimal>
): Map<String, AssetPrice> = assets.mapNotNull { asset ->
    pricesByMarketDataSymbol[asset.marketDataSymbol]?.let { price ->
        asset.id to AssetPrice(
            assetId = asset.id,
            currency = asset.quoteCurrency,
            price = price
        )
    }
}.toMap()
