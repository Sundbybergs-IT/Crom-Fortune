package com.sundbybergsit.cromfortune.ui.notifications

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.ui.home.NotificationAdapterItemUtil
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class NotificationsViewModel : ViewModel() {

    private val _notifications = MutableLiveData<NotificationsViewState>()
    val notifications: LiveData<NotificationsViewState> = _notifications

    fun refreshNew(context: Context) {
        viewModelScope.launch {
            val notifications = NotificationsRepositoryImpl(context).list().filter { notificationMessage ->
                LocalDate.now().isEqual(Instant.ofEpochMilli(notificationMessage.dateInMillis).atZone(ZoneId.systemDefault()).toLocalDate()) }
                    .sortedByDescending {
                notificationMessage -> notificationMessage.dateInMillis }
            if (notifications.isEmpty()) {
                _notifications.postValue(NotificationsViewState.HasNoNewNotifications(R.string.generic_error_empty))
            } else {
                _notifications.postValue(NotificationsViewState.HasNotifications(R.string.notifications_title,
                        NotificationAdapterItemUtil.convertToAdapterItems(notifications)))
            }
        }
    }

    fun refreshOld(context: Context) {
        viewModelScope.launch {
            val notifications = NotificationsRepositoryImpl(context).list()
                    .filter { notificationMessage ->
                        Instant.ofEpochMilli(notificationMessage.dateInMillis).atZone(ZoneId.systemDefault())
                                .toLocalDate().isBefore(LocalDate.now(ZoneId.systemDefault()))
                    }.sortedByDescending { notificationMessage -> notificationMessage.dateInMillis }
            if (notifications.isEmpty()) {
                _notifications.postValue(NotificationsViewState.HasNoOldNotifications(R.string.generic_error_empty))
            } else {
                _notifications.postValue(NotificationsViewState.HasNotifications(R.string.notifications_title,
                        NotificationAdapterItemUtil.convertToAdapterItems(notifications)))
            }
        }
    }

    fun clearNotifications(context: Context) {
        NotificationsRepositoryImpl(context).clear()
        _notifications.postValue(NotificationsViewState.HasNoOldNotifications(R.string.generic_error_empty))
        _notifications.postValue(NotificationsViewState.HasNoNewNotifications(R.string.generic_error_empty))
    }

}
