package com.sundbybergsit.cromfortune.ui.notifications

import androidx.annotation.StringRes
import com.sundbybergsit.cromfortune.ui.AdapterItem

sealed class NotificationsViewState {

    data class HasNotifications(@StringRes val textResId: Int, val adapterItems: List<AdapterItem>) : NotificationsViewState()

    data class HasNoNotifications(@StringRes val textResId: Int) : NotificationsViewState()

}
