package com.sundbybergsit.cromfortune.main.ui.home

import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.ktx.AppUpdateResult
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.lang.reflect.Proxy
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class InAppUpdateHandlerTest {

    @Test
    fun `non-downloaded update does not install or show snackbar`() = runTest {
        var snackbarShown = false
        var installationStarted = false

        handleInAppUpdateResult(
            updateResult = AppUpdateResult.NotAvailable,
            downloadedMessage = DOWNLOADED_MESSAGE,
            installActionLabel = INSTALL_LABEL,
            showSnack = { _, _ -> snackbarShown = true },
            completeUpdate = { installationStarted = true },
        )

        assertFalse(snackbarShown)
        assertFalse(installationStarted)
    }

    @Test
    fun `downloaded update shows install action and waits for click before installing`() = runTest {
        var shownMessage: String? = null
        var shownAction: Pair<String, suspend () -> Unit>? = null
        var installationStarted = false

        handleInAppUpdateResult(
            updateResult = AppUpdateResult.Downloaded(fakeAppUpdateManager()),
            downloadedMessage = DOWNLOADED_MESSAGE,
            installActionLabel = INSTALL_LABEL,
            showSnack = { message, action ->
                shownMessage = message
                shownAction = action
            },
            completeUpdate = { installationStarted = true },
        )

        assertEquals(DOWNLOADED_MESSAGE, shownMessage)
        assertEquals(INSTALL_LABEL, shownAction?.first)
        assertFalse(installationStarted)

        shownAction?.second?.invoke()

        assertTrue(installationStarted)
    }

    private fun fakeAppUpdateManager(): AppUpdateManager =
        Proxy.newProxyInstance(
            AppUpdateManager::class.java.classLoader,
            arrayOf(AppUpdateManager::class.java),
        ) { _, _, _ -> null } as AppUpdateManager

    private companion object {
        const val DOWNLOADED_MESSAGE = "Update downloaded"
        const val INSTALL_LABEL = "Install"
    }
}
