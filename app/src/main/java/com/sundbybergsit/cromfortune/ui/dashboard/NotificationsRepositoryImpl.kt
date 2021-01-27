package com.sundbybergsit.cromfortune.ui.dashboard

import android.content.Context
import android.content.SharedPreferences
import com.sundbybergsit.cromfortune.ui.notifications.NotificationMessage
import com.sundbybergsit.cromfortune.ui.notifications.NotificationsRepository

const val PREFERENCES_NAME = "Notifications"

class NotificationsRepositoryImpl(context: Context, private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)) : NotificationsRepository {

    override fun list(): Set<NotificationMessage> {
        val set = mutableSetOf<NotificationMessage>()
        for (entry in sharedPreferences.all) {
            set.add(NotificationMessage(entry.key.toLong(), entry.value as String))
        }
        return set
    }

    override fun add(notificationMessage : NotificationMessage) {
        sharedPreferences.edit().putString(notificationMessage.dateInMillis.toString(), notificationMessage.message).apply()
    }

    override fun remove(notificationMessage: NotificationMessage) {
        sharedPreferences.edit().remove(notificationMessage.dateInMillis.toString()).apply()
    }

}
