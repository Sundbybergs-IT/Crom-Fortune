package com.sundbybergsit.cromfortune.main

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.time.Instant

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class AssetRefreshStatusRepositoryTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Before
    fun setUp() {
        context.getSharedPreferences("AssetRefreshStatus", Context.MODE_PRIVATE).edit().clear().commit()
        AssetRefreshStatusRepository.init(context)
    }

    @Test
    fun `successful refresh status survives repository reinitialization`() {
        val attemptedAt = Instant.parse("2026-09-30T08:00:00Z")
        val completedAt = Instant.parse("2026-09-30T08:00:05Z")

        AssetRefreshStatusRepository.recordAttempt(RefreshTrigger.BACKGROUND, attemptedAt)
        AssetRefreshStatusRepository.recordSuccess(
            successfulAssets = 12,
            failedAssets = 2,
            completedAt = completedAt
        )
        AssetRefreshStatusRepository.init(context)

        val status = AssetRefreshStatusRepository.status.value
        assertFalse(status.isRefreshing)
        assertEquals(attemptedAt, status.lastAttemptAt)
        assertEquals(completedAt, status.lastSuccessfulAt)
        assertEquals(12, status.successfulAssets)
        assertEquals(2, status.failedAssets)
        assertEquals(RefreshTrigger.BACKGROUND, status.trigger)
    }
}
