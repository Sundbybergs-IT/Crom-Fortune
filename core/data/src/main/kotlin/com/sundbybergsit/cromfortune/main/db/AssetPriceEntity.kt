package com.sundbybergsit.cromfortune.main.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.sundbybergsit.cromfortune.domain.AssetPrice
import java.util.Currency

@Entity(tableName = "asset_prices")
data class AssetPriceEntity(
    @PrimaryKey val assetId: String,
    val currencyCode: String,
    val price: Double,
    val lastUpdatedAt: Long,
    val isStale: Boolean
) {
    fun toDomain(): AssetPrice = AssetPrice(
        assetId = assetId,
        currency = Currency.getInstance(currencyCode),
        price = price
    )

    companion object {
        fun fromDomain(assetPrice: AssetPrice, lastUpdatedAt: Long, isStale: Boolean): AssetPriceEntity =
            AssetPriceEntity(
                assetId = assetPrice.assetId,
                currencyCode = assetPrice.currency.currencyCode,
                price = assetPrice.price.toDouble(),
                lastUpdatedAt = lastUpdatedAt,
                isStale = isStale
            )
    }
}

@Dao
interface AssetPriceDao {
    @Query("SELECT * FROM asset_prices")
    fun getAllAssetPrices(): List<AssetPriceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(assetPrices: List<AssetPriceEntity>)

    @Query("DELETE FROM asset_prices")
    fun deleteAll()
}
