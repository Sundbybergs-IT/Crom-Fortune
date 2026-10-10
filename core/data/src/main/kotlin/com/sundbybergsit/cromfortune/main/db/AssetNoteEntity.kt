package com.sundbybergsit.cromfortune.main.db

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Entity(tableName = "asset_notes")
data class AssetNoteEntity(
    @PrimaryKey val assetId: String,
    val text: String
)

@Dao
interface AssetNoteDao {
    @Query("SELECT * FROM asset_notes")
    fun getAllNotes(): List<AssetNoteEntity>

    @Query("SELECT * FROM asset_notes WHERE assetId = :assetId")
    fun getNote(assetId: String): AssetNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: AssetNoteEntity)

    @Query("DELETE FROM asset_notes WHERE assetId = :assetId")
    fun delete(assetId: String)
}
