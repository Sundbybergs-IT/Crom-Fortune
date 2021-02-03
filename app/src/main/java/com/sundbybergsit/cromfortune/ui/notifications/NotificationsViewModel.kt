package com.sundbybergsit.cromfortune.ui.notifications

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.ui.dashboard.NotificationsRepositoryImpl
import com.sundbybergsit.cromfortune.ui.home.AdapterItem
import com.sundbybergsit.cromfortune.ui.home.NotificationAdapterItemUtil
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {


    private val _notifications = MutableLiveData<ViewState>()
    val notifications: LiveData<ViewState> = _notifications

    fun refresh(context: Context) {
        viewModelScope.launch {
            val notifications = NotificationsRepositoryImpl(context).list().sortedByDescending {
                notificationMessage -> notificationMessage.dateInMillis }
            if (notifications.isEmpty()) {
                _notifications.postValue(ViewState.HasNoNotifications(R.string.generic_error_empty))
            } else {
                _notifications.postValue(ViewState.HasNotifications(R.string.notifications_title,
                        NotificationAdapterItemUtil.convertToAdapterItems(notifications)))
            }
        }
    }

    fun clearNotifications(context: Context) {
        NotificationsRepositoryImpl(context).clear()
        _notifications.postValue(ViewState.HasNoNotifications(R.string.generic_error_empty))
    }

    sealed class ViewState {

        data class HasNotifications(@StringRes val textResId: Int, val adapterItems: List<AdapterItem>) : ViewState()

        data class HasNoNotifications(@StringRes val textResId: Int) : ViewState()

    }

}
