package com.sundbybergsit.cromfortune.main.notifications

import android.content.Context
import android.content.SharedPreferences
import com.sundbybergsit.cromfortune.domain.notifications.NotificationMessage
import com.sundbybergsit.cromfortune.domain.notifications.NotificationsRepository
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

const val PREFERENCES_NAME = "Notifications"

class NotificationsRepositoryImpl(context: Context, private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)) : NotificationsRepository {

    override fun list(): Set<NotificationMessage> {
        val set = mutableSetOf<NotificationMessage>()
        for (entry in sharedPreferences.all) {
            val persistedValue = entry.value as String
            val notification = try {
                Json.decodeFromString<NotificationMessage>(persistedValue)
            } catch (_: SerializationException) {
                NotificationMessage(entry.key.toLong(), persistedValue)
            } catch (_: IllegalArgumentException) {
                NotificationMessage(entry.key.toLong(), persistedValue)
            }
            set.add(notification)
        }
        return set
    }

    override fun add(notificationMessage : NotificationMessage) {
        sharedPreferences.edit()
            .putString(notificationMessage.dateInMillis.toString(), Json.encodeToString(notificationMessage))
            .apply()
    }

    override fun remove(notificationMessage: NotificationMessage) {
        sharedPreferences.edit().remove(notificationMessage.dateInMillis.toString()).apply()
    }

    override fun clear() {
        sharedPreferences.edit().clear().apply()
    }

}
