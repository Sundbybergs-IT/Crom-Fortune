package com.sundbybergsit.cromfortune.ui.settings

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class StockMuteSettingsRepositoryTest {

    @Before
    fun setUp() {
        StockMuteSettingsRepository.init(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun `isMuted - when stock symbol is missing - returns false`() {
        val muted = StockMuteSettingsRepository.isMuted("Unknown stock")

        assertFalse(muted)
    }

    @Test
    fun `isMuted - when stock symbol is unmuted - returns true`() {
        StockMuteSettingsRepository.mute("Unmuted stock")
        StockMuteSettingsRepository.unmute("Unmuted stock")

        val muted = StockMuteSettingsRepository.isMuted("Unmuted stock")

        assertFalse(muted)
    }

    @Test
    fun `isMuted - when stock symbol is muted - returns true`() {
        StockMuteSettingsRepository.mute("Muted stock")

        val muted = StockMuteSettingsRepository.isMuted("Muted stock")

        assertTrue(muted)
    }

}
