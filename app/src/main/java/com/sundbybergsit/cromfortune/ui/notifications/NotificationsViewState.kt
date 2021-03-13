package com.sundbybergsit.cromfortune.ui.notifications

import androidx.annotation.StringRes
import com.sundbybergsit.cromfortune.ui.home.AdapterItem

sealed class NotificationsViewState {

    data class HasNotifications(@StringRes val textResId: Int, val adapterItems: List<AdapterItem>) : NotificationsViewState()

    data class HasNoNewNotifications(@StringRes val textResId: Int) : NotificationsViewState()

    data class HasNoOldNotifications(@StringRes val textResId: Int) : NotificationsViewState()

}
