package com.sundbybergsit.cromfortune.ui.home

import com.sundbybergsit.cromfortune.ui.notifications.NotificationMessage

internal object NotificationAdapterItemUtil {

    @JvmStatic
    fun convertToAdapterItems(list: Iterable<NotificationMessage>): List<AdapterItem> {
        val result: MutableList<AdapterItem> = ArrayList()
        for (connection in list) {
            val pdAdapterItem = NotificationAdapterItem(connection)
            result.add(pdAdapterItem)
        }
        return result
    }

}
