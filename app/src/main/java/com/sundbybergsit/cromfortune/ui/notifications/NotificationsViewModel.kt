package com.sundbybergsit.cromfortune.ui.notifications

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sundbybergsit.cromfortune.domain.notifications.NotificationMessage
import com.sundbybergsit.cromfortune.domain.notifications.NotificationsRepository
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class NotificationsViewModel(private val notificationsRepository: NotificationsRepository) : ViewModel() {

    private val _newNotifications : MutableState<Collection<NotificationMessage>> = mutableStateOf(listOf())
    val newNotifications: State<Collection<NotificationMessage>> = _newNotifications
    private val _oldNotifications : MutableState<Collection<NotificationMessage>> = mutableStateOf(listOf())
    val oldNotifications: State<Collection<NotificationMessage>> = _oldNotifications

    init {
        viewModelScope.launch {
            val notifications = notificationsRepository.list().filter { notificationMessage ->
                LocalDate.now().isEqual(Instant.ofEpochMilli(notificationMessage.dateInMillis).atZone(ZoneId.systemDefault()).toLocalDate()) }
                .sortedByDescending {
                        notificationMessage -> notificationMessage.dateInMillis }
            if (notifications.isEmpty()) {
                _newNotifications.value = listOf()
//                _newNotifications.postValue(NotificationsViewState.HasNoNotifications(R.string.generic_error_empty))
            } else {
                _newNotifications.value = notifications
//                _newNotifications.postValue(NotificationsViewState.HasNotifications(R.string.notifications_title,
//                        NotificationAdapterItemUtil.convertToAdapterItems(notifications)))
            }

            val notifications2 = notificationsRepository.list()
                .filter { notificationMessage ->
                    Instant.ofEpochMilli(notificationMessage.dateInMillis).atZone(ZoneId.systemDefault())
                        .toLocalDate().isBefore(LocalDate.now(ZoneId.systemDefault()))
                }.sortedByDescending { notificationMessage -> notificationMessage.dateInMillis }
            if (notifications2.isEmpty()) {
                _newNotifications.value = listOf()
//                _oldNotifications.postValue(NotificationsViewState.HasNoNotifications(R.string.generic_error_empty))
            } else {
                _oldNotifications.value = notifications2
//                _oldNotifications.postValue(NotificationsViewState.HasNotifications(R.string.notifications_title,
//                        NotificationAdapterItemUtil.convertToAdapterItems(notifications)))
            }
        }
    }

    fun clearNotifications() {
        notificationsRepository.clear()
//        _oldNotifications.postValue(NotificationsViewState.HasNoNotifications(R.string.generic_error_empty))
//        _newNotifications.postValue(NotificationsViewState.HasNoNotifications(R.string.generic_error_empty))
    }

}
