package com.sundbybergsit.cromfortune.main

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Config.OLDEST_SDK])
class ReviewPromptPolicyTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val preferences by lazy {
        context.getSharedPreferences("ReviewPromptPolicyTest", Context.MODE_PRIVATE)
    }

    @Before
    fun clearPreferences() {
        preferences.edit().clear().commit()
    }

    @Test
    fun `requires engagement and seven days since installation`() {
        val day = 24L * 60L * 60L * 1_000L
        val policy = ReviewPromptPolicy(preferences, firstInstallTimeMillis = 0L)

        assertFalse(policy.isEligible(assetCount = 4, nowMillis = 8L * day))
        assertFalse(policy.isEligible(assetCount = 5, nowMillis = 6L * day))
        assertTrue(policy.isEligible(assetCount = 5, nowMillis = 7L * day))
    }

    @Test
    fun `waits 120 days after an attempt`() {
        val day = 24L * 60L * 60L * 1_000L
        val policy = ReviewPromptPolicy(preferences, firstInstallTimeMillis = 0L)

        policy.recordAttempt(nowMillis = 10L * day)

        assertFalse(policy.isEligible(assetCount = 5, nowMillis = 129L * day))
        assertTrue(policy.isEligible(assetCount = 5, nowMillis = 130L * day))
    }

    @Test
    fun `allows no more than three attempts`() {
        val day = 24L * 60L * 60L * 1_000L
        val policy = ReviewPromptPolicy(preferences, firstInstallTimeMillis = 0L)

        policy.recordAttempt(nowMillis = 10L * day)
        policy.recordAttempt(nowMillis = 130L * day)
        policy.recordAttempt(nowMillis = 250L * day)

        assertFalse(policy.isEligible(assetCount = 5, nowMillis = 500L * day))
    }
}
