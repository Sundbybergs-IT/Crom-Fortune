@file:Suppress("unused")

package com.sundbybergsit.cromfortune

import android.app.Application
import com.sundbybergsit.cromfortune.ui.notifications.NotificationUtil

class CromFortuneApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationUtil.createChannel(applicationContext)
    }

}
