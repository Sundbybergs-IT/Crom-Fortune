package com.sundbybergsit.cromfortune.main.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

@Entity(tableName = "portfolios")
data class PortfolioEntity(
    @PrimaryKey val name: String
)

@Dao
interface PortfolioDao {
    @Query("SELECT name FROM portfolios")
    fun getAllPortfoliosList(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(portfolio: PortfolioEntity)

    @Query("DELETE FROM portfolios WHERE name = :name")
    fun delete(name: String)
}
