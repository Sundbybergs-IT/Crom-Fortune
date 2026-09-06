package com.sundbybergsit.cromfortune.main

import com.sundbybergsit.cromfortune.domain.AssetPrice
import com.sundbybergsit.cromfortune.domain.TradableAsset
import yahoofinance.get
import yahoofinance.getFxHax
import java.util.Currency

interface MarketDataClient {

    fun getRateInSek(currency: Currency): Double

    fun getPrices(assets: Collection<TradableAsset>): Map<String, AssetPrice>
}

object YahooMarketDataClient : MarketDataClient {

    override fun getRateInSek(currency: Currency): Double =
        getFxHax("${currency.currencyCode}SEK=X")?.price?.toDouble() ?: 1.0

    override fun getPrices(assets: Collection<TradableAsset>): Map<String, AssetPrice> {
        val marketDataPrices = get(assets.map(TradableAsset::marketDataSymbol).toTypedArray())
            .mapNotNull { (marketDataSymbol, stock) ->
                stock.quote?.price?.toDouble()?.let { price -> marketDataSymbol to price }
            }.toMap()
        return mapMarketDataPrices(assets, marketDataPrices)
    }
}

internal fun mapMarketDataPrices(
    assets: Collection<TradableAsset>,
    pricesByMarketDataSymbol: Map<String, Double>
): Map<String, AssetPrice> = assets.mapNotNull { asset ->
    pricesByMarketDataSymbol[asset.marketDataSymbol]?.let { price ->
        asset.id to AssetPrice(
            assetId = asset.id,
            currency = asset.quoteCurrency,
            price = price
        )
    }
}.toMap()
