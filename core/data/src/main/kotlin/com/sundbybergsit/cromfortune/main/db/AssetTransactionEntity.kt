package com.sundbybergsit.cromfortune.main.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.sundbybergsit.cromfortune.domain.AssetTransaction
import com.sundbybergsit.cromfortune.domain.AssetType
import com.sundbybergsit.cromfortune.domain.TransactionAction
import java.math.BigDecimal

@Entity(
    tableName = "asset_transactions",
    indices = [Index("portfolioName"), Index("assetId")]
)
data class AssetTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val portfolioName: String,
    val assetId: String,
    val assetType: String,
    val symbol: String,
    val displayName: String,
    val quoteCurrencyCode: String,
    val action: String,
    val dateInMillis: Long,
    val unitPrice: String,
    val commissionFee: String,
    val quantity: String,
    val schemaVersion: Int
) {
    fun toDomain(): AssetTransaction = AssetTransaction(
        assetId = assetId,
        assetType = AssetType.valueOf(assetType),
        symbol = symbol,
        displayName = displayName,
        quoteCurrencyCode = quoteCurrencyCode,
        action = TransactionAction.valueOf(action),
        dateInMillis = dateInMillis,
        unitPrice = BigDecimal(unitPrice),
        commissionFee = BigDecimal(commissionFee),
        quantity = BigDecimal(quantity),
        schemaVersion = schemaVersion
    )

    companion object {
        fun fromDomain(portfolioName: String, transaction: AssetTransaction): AssetTransactionEntity =
            AssetTransactionEntity(
                portfolioName = portfolioName,
                assetId = transaction.assetId,
                assetType = transaction.assetType.name,
                symbol = transaction.symbol,
                displayName = transaction.displayName,
                quoteCurrencyCode = transaction.quoteCurrencyCode,
                action = transaction.action.name,
                dateInMillis = transaction.dateInMillis,
                unitPrice = transaction.unitPrice.toPlainString(),
                commissionFee = transaction.commissionFee.toPlainString(),
                quantity = transaction.quantity.toPlainString(),
                schemaVersion = transaction.schemaVersion
            )
    }
}

@Dao
interface AssetTransactionDao {
    @Query("SELECT * FROM asset_transactions WHERE portfolioName = :portfolioName")
    fun getTransactionsForPortfolio(portfolioName: String): List<AssetTransactionEntity>

    @Query("SELECT * FROM asset_transactions WHERE portfolioName = :portfolioName AND assetId = :assetId")
    fun getTransactionsForAsset(portfolioName: String, assetId: String): List<AssetTransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(transactions: List<AssetTransactionEntity>)

    @Query("DELETE FROM asset_transactions WHERE portfolioName = :portfolioName AND assetId = :assetId")
    fun deleteForAsset(portfolioName: String, assetId: String)

    @Query("DELETE FROM asset_transactions WHERE portfolioName = :portfolioName")
    fun deleteAllForPortfolio(portfolioName: String)
}
