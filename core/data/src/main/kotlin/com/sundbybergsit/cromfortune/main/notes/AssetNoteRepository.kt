package com.sundbybergsit.cromfortune.main.notes

import android.content.Context
import com.sundbybergsit.cromfortune.main.db.AssetNoteEntity
import com.sundbybergsit.cromfortune.main.db.CromFortuneDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable

const val MAX_ASSET_NOTE_LENGTH = 500

@Serializable
data class AssetNote(
    val portfolioName: String,
    val assetId: String,
    val text: String
)

object AssetNoteRepository {
    private lateinit var appContext: Context
    private val _notes = MutableStateFlow<Map<Pair<String, String>, AssetNote>>(emptyMap())
    val notes: StateFlow<Map<Pair<String, String>, AssetNote>> = _notes.asStateFlow()

    fun init(context: Context) {
        appContext = context.applicationContext
        reload()
    }

    fun get(portfolioName: String, assetId: String): AssetNote? =
        notes.value[portfolioName to assetId]

    fun save(portfolioName: String, assetId: String, text: String) {
        require(text.isNotBlank()) { "An asset note must not be blank" }
        require(text.length <= MAX_ASSET_NOTE_LENGTH) {
            "An asset note must not exceed $MAX_ASSET_NOTE_LENGTH characters"
        }
        val dao = CromFortuneDatabase.getInstance(appContext).assetNoteDao()
        dao.insert(AssetNoteEntity(assetId = storageKey(portfolioName, assetId), text = text))
        reload()
    }

    fun delete(portfolioName: String, assetId: String) {
        val dao = CromFortuneDatabase.getInstance(appContext).assetNoteDao()
        dao.delete(storageKey(portfolioName, assetId))
        reload()
    }

    private fun reload() {
        val dao = CromFortuneDatabase.getInstance(appContext).assetNoteDao()
        val list = dao.getAllNotes()
        _notes.value = list.mapNotNull { entity ->
            val parts = entity.assetId.split("\u001f", limit = 2)
            if (parts.size == 2) {
                AssetNote(parts[0], parts[1], entity.text)
            } else {
                null
            }
        }.associateBy { note -> note.portfolioName to note.assetId }
    }

    private fun storageKey(portfolioName: String, assetId: String) =
        "$portfolioName\u001f$assetId"
}
