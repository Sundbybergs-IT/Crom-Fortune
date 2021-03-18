package com.sundbybergsit.cromfortune.ui.notifications

import com.sundbybergsit.cromfortune.notifications.NotificationMessage
import com.sundbybergsit.cromfortune.ui.AdapterItem

data class NotificationAdapterItem(val notificationMessage: NotificationMessage) : AdapterItem {

    override fun isContentTheSame(item: AdapterItem): Boolean {
        return item is NotificationAdapterItem && notificationMessage == item.notificationMessage
    }

}
