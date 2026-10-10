package com.sundbybergsit.cromfortune.main.notes

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.sundbybergsit.cromfortune.main.db.CromFortuneDatabase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class AssetNoteRepositoryTest {
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        val db = CromFortuneDatabase.getInstance(context)
        db.assetNoteDao().getAllNotes().forEach { db.assetNoteDao().delete(it.assetId) }
        AssetNoteRepository.init(context)
    }

    @Test
    fun `save creates and edits the single note for an asset`() {
        AssetNoteRepository.save("Mine", "stock:ERIC-B.ST", "First")
        AssetNoteRepository.save("Mine", "stock:ERIC-B.ST", "Updated")

        assertEquals("Updated", AssetNoteRepository.get("Mine", "stock:ERIC-B.ST")?.text)
        assertEquals(1, AssetNoteRepository.notes.value.size)
    }

    @Test
    fun `notes are scoped by portfolio`() {
        AssetNoteRepository.save("Mine", "stock:ERIC-B.ST", "One")
        AssetNoteRepository.save("Other", "stock:ERIC-B.ST", "Two")

        assertEquals("One", AssetNoteRepository.get("Mine", "stock:ERIC-B.ST")?.text)
        assertEquals("Two", AssetNoteRepository.get("Other", "stock:ERIC-B.ST")?.text)
    }

    @Test
    fun `delete removes the note`() {
        AssetNoteRepository.save("Mine", "crypto:BTC-USD", "Watch this")
        AssetNoteRepository.delete("Mine", "crypto:BTC-USD")

        assertNull(AssetNoteRepository.get("Mine", "crypto:BTC-USD"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `save rejects notes longer than 500 characters`() {
        AssetNoteRepository.save("Mine", "stock:ERIC-B.ST", "x".repeat(MAX_ASSET_NOTE_LENGTH + 1))
    }
}
