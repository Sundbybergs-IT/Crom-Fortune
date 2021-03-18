package com.sundbybergsit.cromfortune.notifications

interface NotificationsRepository {

    fun list(): Set<NotificationMessage>

    fun add(notificationMessage: NotificationMessage)

    fun remove(notificationMessage: NotificationMessage)

    fun clear()

}
