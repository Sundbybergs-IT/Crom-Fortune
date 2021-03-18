package com.sundbybergsit.cromfortune.notifications

import android.content.Context
import android.content.SharedPreferences

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

    override fun clear() {
        sharedPreferences.edit().clear().apply()
    }

}
