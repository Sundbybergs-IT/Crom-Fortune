package com.sundbybergsit.cromfortune.domain

interface AssetPriceApi {

    fun putAssetPrices(assetPrices: Set<AssetPrice>)

    fun getAssetPrice(assetId: String): AssetPrice?
}

