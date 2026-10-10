package com.sundbybergsit.cromfortune.main.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.StockOrder
import java.math.BigDecimal

@Entity(
    tableName = "stock_orders",
    indices = [Index("portfolioName"), Index("assetId")]
)
data class StockOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val portfolioName: String,
    val assetId: String,
    val orderAction: String,
    val currency: String,
    val dateInMillis: Long,
    val name: String,
    val pricePerStock: Double,
    val commissionFee: Double,
    val quantity: String,
    val assetType: String,
    val schemaVersion: Int
) {
    fun toDomain(): StockOrder = StockOrder(
        orderAction = orderAction,
        currency = currency,
        dateInMillis = dateInMillis,
        name = name,
        pricePerStock = pricePerStock,
        commissionFee = commissionFee,
        quantity = BigDecimal(quantity),
        assetId = assetId,
        assetType = AssetType.valueOf(assetType),
        schemaVersion = schemaVersion
    )

    companion object {
        fun fromDomain(portfolioName: String, order: StockOrder): StockOrderEntity =
            StockOrderEntity(
                portfolioName = portfolioName,
                assetId = order.assetId,
                orderAction = order.orderAction,
                currency = order.currency,
                dateInMillis = order.dateInMillis,
                name = order.name,
                pricePerStock = order.pricePerStock,
                commissionFee = order.commissionFee,
                quantity = order.quantity.toPlainString(),
                assetType = order.assetType.name,
                schemaVersion = order.schemaVersion
            )
    }
}

@Dao
interface StockOrderDao {
    @Query("SELECT * FROM stock_orders WHERE portfolioName = :portfolioName")
    fun getOrdersForPortfolio(portfolioName: String): List<StockOrderEntity>

    @Query("SELECT * FROM stock_orders WHERE portfolioName = :portfolioName AND (assetId = :assetId OR name = :assetId)")
    fun getOrdersForAsset(portfolioName: String, assetId: String): List<StockOrderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(orders: List<StockOrderEntity>)

    @Query("DELETE FROM stock_orders WHERE portfolioName = :portfolioName AND (assetId = :assetId OR name = :assetId)")
    fun deleteForAsset(portfolioName: String, assetId: String)

    @Query("DELETE FROM stock_orders WHERE portfolioName = :portfolioName")
    fun deleteAllForPortfolio(portfolioName: String)
}
