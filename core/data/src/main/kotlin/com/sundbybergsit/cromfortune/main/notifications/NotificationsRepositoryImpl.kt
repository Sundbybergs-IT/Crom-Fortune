package com.sundbybergsit.cromfortune.main.notifications

import android.content.Context
import com.sundbybergsit.cromfortune.domain.notifications.NotificationMessage
import com.sundbybergsit.cromfortune.domain.notifications.NotificationsRepository
import com.sundbybergsit.cromfortune.main.db.CromFortuneDatabase
import com.sundbybergsit.cromfortune.main.db.NotificationDao
import com.sundbybergsit.cromfortune.main.db.NotificationEntity

class NotificationsRepositoryImpl(
    context: Context,
    private val dao: NotificationDao = CromFortuneDatabase.getInstance(context).notificationDao()
) : NotificationsRepository {

    override fun list(): Set<NotificationMessage> {
        return dao.getAllNotifications().map { it.toDomain() }.toSet()
    }

    override fun add(notificationMessage: NotificationMessage) {
        dao.insert(NotificationEntity.fromDomain(notificationMessage))
    }

    override fun remove(notificationMessage: NotificationMessage) {
        dao.delete(0L, notificationMessage.dateInMillis, notificationMessage.message)
    }

    override fun clear() {
        dao.deleteAll()
    }

}
