package com.sundbybergsit.cromfortune.main

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant

enum class RefreshTrigger {
    MANUAL,
    BACKGROUND
}

data class AssetRefreshStatus(
    val isRefreshing: Boolean = false,
    val lastAttemptAt: Instant? = null,
    val lastSuccessfulAt: Instant? = null,
    val successfulAssets: Int = 0,
    val failedAssets: Int = 0,
    val lastError: String? = null,
    val trigger: RefreshTrigger? = null
)

object AssetRefreshStatusRepository {

    private const val PREFERENCES_NAME = "AssetRefreshStatus"
    private const val LAST_ATTEMPT_AT = "lastAttemptAt"
    private const val LAST_SUCCESSFUL_AT = "lastSuccessfulAt"
    private const val SUCCESSFUL_ASSETS = "successfulAssets"
    private const val FAILED_ASSETS = "failedAssets"
    private const val LAST_ERROR = "lastError"
    private const val TRIGGER = "trigger"

    private var applicationContext: Context? = null
    private val _status = MutableStateFlow(AssetRefreshStatus())
    val status: StateFlow<AssetRefreshStatus> = _status.asStateFlow()

    @Synchronized
    fun init(context: Context) {
        applicationContext = context.applicationContext
        val preferences = preferences()
        _status.value = AssetRefreshStatus(
            lastAttemptAt = preferences.getLong(LAST_ATTEMPT_AT, 0L).toInstantOrNull(),
            lastSuccessfulAt = preferences.getLong(LAST_SUCCESSFUL_AT, 0L).toInstantOrNull(),
            successfulAssets = preferences.getInt(SUCCESSFUL_ASSETS, 0),
            failedAssets = preferences.getInt(FAILED_ASSETS, 0),
            lastError = preferences.getString(LAST_ERROR, null),
            trigger = preferences.getString(TRIGGER, null)?.let { storedValue ->
                runCatching { RefreshTrigger.valueOf(storedValue) }.getOrNull()
            }
        )
    }

    @Synchronized
    fun recordAttempt(trigger: RefreshTrigger, attemptedAt: Instant = Instant.now()) {
        ensureInitialized()
        _status.value = _status.value.copy(
            isRefreshing = true,
            lastAttemptAt = attemptedAt,
            lastError = null,
            trigger = trigger
        )
        persist()
    }

    @Synchronized
    fun recordSuccess(
        successfulAssets: Int,
        failedAssets: Int,
        completedAt: Instant = Instant.now()
    ) {
        ensureInitialized()
        _status.value = _status.value.copy(
            isRefreshing = false,
            lastSuccessfulAt = completedAt,
            successfulAssets = successfulAssets,
            failedAssets = failedAssets,
            lastError = null
        )
        persist()
    }

    @Synchronized
    fun recordFailure(error: Throwable) {
        ensureInitialized()
        _status.value = _status.value.copy(
            isRefreshing = false,
            lastError = error.message ?: error.javaClass.simpleName
        )
        persist()
    }

    private fun persist() {
        val value = _status.value
        preferences().edit()
            .putLong(LAST_ATTEMPT_AT, value.lastAttemptAt?.toEpochMilli() ?: 0L)
            .putLong(LAST_SUCCESSFUL_AT, value.lastSuccessfulAt?.toEpochMilli() ?: 0L)
            .putInt(SUCCESSFUL_ASSETS, value.successfulAssets)
            .putInt(FAILED_ASSETS, value.failedAssets)
            .putString(LAST_ERROR, value.lastError)
            .putString(TRIGGER, value.trigger?.name)
            .apply()
    }

    private fun preferences() = checkNotNull(applicationContext) {
        "AssetRefreshStatusRepository.init must be called before use"
    }.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    private fun ensureInitialized() {
        checkNotNull(applicationContext) { "AssetRefreshStatusRepository has not been initialized" }
    }

    private fun Long.toInstantOrNull(): Instant? = takeIf { it > 0L }?.let(Instant::ofEpochMilli)
}
