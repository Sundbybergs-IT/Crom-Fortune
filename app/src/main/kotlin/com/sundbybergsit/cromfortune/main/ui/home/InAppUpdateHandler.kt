package com.sundbybergsit.cromfortune.main.ui.home

import com.google.android.play.core.ktx.AppUpdateResult
import com.sundbybergsit.cromfortune.main.DialogHandler

internal fun handleInAppUpdateResult(
    updateResult: AppUpdateResult,
    downloadedMessage: String,
    installActionLabel: String,
    showSnack: (String, Pair<String, suspend () -> Unit>) -> Unit = DialogHandler::showSnack,
    completeUpdate: suspend (AppUpdateResult.Downloaded) -> Unit = { it.completeUpdate() },
) {
    val downloadedUpdate = updateResult as? AppUpdateResult.Downloaded ?: return

    showSnack(
        downloadedMessage,
        Pair(installActionLabel) {
            completeUpdate(downloadedUpdate)
        },
    )
}
