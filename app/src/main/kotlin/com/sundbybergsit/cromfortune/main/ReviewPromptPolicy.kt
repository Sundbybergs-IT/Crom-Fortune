package com.sundbybergsit.cromfortune.main

import android.content.SharedPreferences

internal class ReviewPromptPolicy(
    private val preferences: SharedPreferences,
    private val firstInstallTimeMillis: Long
) {
    fun isEligible(assetCount: Int, nowMillis: Long = System.currentTimeMillis()): Boolean {
        if (assetCount < MINIMUM_ASSET_COUNT) return false
        if (nowMillis - firstInstallTimeMillis < MINIMUM_INSTALL_AGE_MILLIS) return false
        if (preferences.getInt(KEY_ATTEMPT_COUNT, 0) >= MAXIMUM_ATTEMPTS) return false

        val lastAttemptMillis = preferences.getLong(KEY_LAST_ATTEMPT_MILLIS, 0L)
        return lastAttemptMillis == 0L || nowMillis - lastAttemptMillis >= ATTEMPT_COOLDOWN_MILLIS
    }

    fun recordAttempt(nowMillis: Long = System.currentTimeMillis()) {
        preferences.edit()
            .putLong(KEY_LAST_ATTEMPT_MILLIS, nowMillis)
            .putInt(KEY_ATTEMPT_COUNT, preferences.getInt(KEY_ATTEMPT_COUNT, 0) + 1)
            .apply()
    }

    companion object {
        const val PREFERENCES_NAME = "ReviewPrompt"

        private const val KEY_ATTEMPT_COUNT = "attemptCount"
        private const val KEY_LAST_ATTEMPT_MILLIS = "lastAttemptMillis"
        private const val MINIMUM_ASSET_COUNT = 5
        private const val MAXIMUM_ATTEMPTS = 3
        private const val DAY_MILLIS = 24L * 60L * 60L * 1_000L
        private const val MINIMUM_INSTALL_AGE_MILLIS = 7L * DAY_MILLIS
        private const val ATTEMPT_COOLDOWN_MILLIS = 120L * DAY_MILLIS
    }
}
