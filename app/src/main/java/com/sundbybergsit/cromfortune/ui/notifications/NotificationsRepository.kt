package com.sundbybergsit.cromfortune.ui.notifications

interface NotificationsRepository {

    fun list(): Set<NotificationMessage>

    fun add(notificationMessage: NotificationMessage)

    fun remove(notificationMessage: NotificationMessage)

}
