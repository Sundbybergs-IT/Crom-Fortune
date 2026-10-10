package com.sundbybergsit.cromfortune.main.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Entity(tableName = "stock_mute_settings")
data class StockMuteSettingsEntity(
    @PrimaryKey val stockSymbol: String,
    val muted: Boolean
)

@Dao
interface StockMuteSettingsDao {
    @Query("SELECT * FROM stock_mute_settings")
    fun getAllMuteSettings(): List<StockMuteSettingsEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(setting: StockMuteSettingsEntity)

    @Query("DELETE FROM stock_mute_settings WHERE stockSymbol = :stockSymbol")
    fun delete(stockSymbol: String)
}
