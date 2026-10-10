package com.sundbybergsit.cromfortune.main.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.sundbybergsit.cromfortune.domain.StockSplit

@Entity(
    tableName = "stock_splits",
    indices = [Index("portfolioName"), Index("name")]
)
data class StockSplitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val portfolioName: String,
    val name: String,
    val dateInMillis: Long,
    val quantity: Int,
    val reverse: Boolean
) {
    fun toDomain(): StockSplit = StockSplit(
        reverse = reverse,
        dateInMillis = dateInMillis,
        name = name,
        quantity = quantity
    )

    companion object {
        fun fromDomain(portfolioName: String, split: StockSplit): StockSplitEntity =
            StockSplitEntity(
                portfolioName = portfolioName,
                name = split.name,
                dateInMillis = split.dateInMillis,
                quantity = split.quantity,
                reverse = split.reverse
            )
    }
}

@Dao
interface StockSplitDao {
    @Query("SELECT * FROM stock_splits WHERE portfolioName = :portfolioName AND name = :stockName")
    fun getSplitsForStock(portfolioName: String, stockName: String): List<StockSplitEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(splits: List<StockSplitEntity>)

    @Query("DELETE FROM stock_splits WHERE portfolioName = :portfolioName AND name = :stockName")
    fun deleteForStock(portfolioName: String, stockName: String)

    @Query("DELETE FROM stock_splits WHERE portfolioName = :portfolioName")
    fun deleteAllForPortfolio(portfolioName: String)
}
