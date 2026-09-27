package com.sundbybergsit.cromfortune.main.notes

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val ASSET_NOTES_PREFERENCES_NAME = "AssetNotes"
const val MAX_ASSET_NOTE_LENGTH = 500

@Serializable
data class AssetNote(
    val portfolioName: String,
    val assetId: String,
    val text: String
)

object AssetNoteRepository {
    private lateinit var preferences: SharedPreferences
    private val _notes = MutableStateFlow<Map<Pair<String, String>, AssetNote>>(emptyMap())
    val notes: StateFlow<Map<Pair<String, String>, AssetNote>> = _notes.asStateFlow()

    fun init(context: Context) {
        preferences = context.getSharedPreferences(ASSET_NOTES_PREFERENCES_NAME, Context.MODE_PRIVATE)
        reload()
    }

    fun get(portfolioName: String, assetId: String): AssetNote? =
        notes.value[portfolioName to assetId]

    fun save(portfolioName: String, assetId: String, text: String) {
        checkInitialized()
        require(text.isNotBlank()) { "An asset note must not be blank" }
        require(text.length <= MAX_ASSET_NOTE_LENGTH) {
            "An asset note must not exceed $MAX_ASSET_NOTE_LENGTH characters"
        }
        val note = AssetNote(portfolioName, assetId, text)
        check(preferences.edit().putString(storageKey(portfolioName, assetId), Json.encodeToString(note)).commit()) {
            "Failed to persist note for $assetId"
        }
        reload()
    }

    fun delete(portfolioName: String, assetId: String) {
        checkInitialized()
        check(preferences.edit().remove(storageKey(portfolioName, assetId)).commit()) {
            "Failed to delete note for $assetId"
        }
        reload()
    }

    private fun reload() {
        _notes.value = preferences.all.values.mapNotNull { value ->
            runCatching { Json.decodeFromString<AssetNote>(value as String) }.getOrNull()
        }.associateBy { note -> note.portfolioName to note.assetId }
    }

    private fun storageKey(portfolioName: String, assetId: String) =
        "$portfolioName\u001f$assetId"

    private fun checkInitialized() {
        check(::preferences.isInitialized) { "AssetNoteRepository has not been initialized" }
    }
}
