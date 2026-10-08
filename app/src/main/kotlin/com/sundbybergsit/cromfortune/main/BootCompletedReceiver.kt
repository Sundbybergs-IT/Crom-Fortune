package com.sundbybergsit.cromfortune.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.sundbybergsit.cromfortune.main.settings.StockRetrievalSettings

class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.i(TAG, "onReceive(${intent.action})")
            val refreshInterval = StockRetrievalSettings(context).timeInterval.value.refreshInterval
            AssetRefreshScheduler.schedule(context, refreshInterval)
        }
    }

}
