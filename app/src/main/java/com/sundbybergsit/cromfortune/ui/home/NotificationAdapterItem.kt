package com.sundbybergsit.cromfortune.ui.home

import com.sundbybergsit.cromfortune.ui.notifications.NotificationMessage

data class NotificationAdapterItem(val notificationMessage: NotificationMessage) : AdapterItem {

    override fun isContentTheSame(item: AdapterItem): Boolean {
        return item is NotificationAdapterItem && notificationMessage == item.notificationMessage
    }

}
