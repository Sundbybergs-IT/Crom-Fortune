package com.sundbybergsit.cromfortune.ui.notifications

import androidx.annotation.StringRes
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.domain.notifications.NotificationMessage
import com.sundbybergsit.cromfortune.domain.notifications.NotificationsRepository
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class NotificationsViewModel(private val notificationsRepository: NotificationsRepository) : ViewModel() {

    private val _selectedTabIndexMutableState : MutableState<Int> = mutableIntStateOf(0)
    val selectedTabIndexMutableState : State<Int> = _selectedTabIndexMutableState

    private val _newNotifications: MutableState<NotificationsViewState> = mutableStateOf(NotificationsViewState(R.string.generic_error_empty, listOf()))
    val newNotifications: State<NotificationsViewState> = _newNotifications
    private val _oldNotifications: MutableState<NotificationsViewState> = mutableStateOf(NotificationsViewState(R.string.generic_error_empty, listOf()))
    val oldNotifications: State<NotificationsViewState> = _oldNotifications

    init {
        viewModelScope.launch {
            val notifications = notificationsRepository.list().filter { notificationMessage ->
                LocalDate.now().isEqual(
                    Instant.ofEpochMilli(notificationMessage.dateInMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                )
            }
                .sortedByDescending { notificationMessage -> notificationMessage.dateInMillis }
            if (notifications.isEmpty()) {
                _newNotifications.value = NotificationsViewState(R.string.generic_error_empty, listOf())
            } else {
                _newNotifications.value = NotificationsViewState(R.string.notifications_title, notifications)
            }

            val notifications2 = notificationsRepository.list()
                .filter { notificationMessage ->
                    Instant.ofEpochMilli(notificationMessage.dateInMillis).atZone(ZoneId.systemDefault())
                        .toLocalDate().isBefore(LocalDate.now(ZoneId.systemDefault()))
                }.sortedByDescending { notificationMessage -> notificationMessage.dateInMillis }
            if (notifications2.isEmpty()) {
                _oldNotifications.value = NotificationsViewState(R.string.generic_error_empty, listOf())
            } else {
                _oldNotifications.value = NotificationsViewState(R.string.notifications_title, notifications2)
            }
        }
    }

    fun selectTab(index: Int) {
        _selectedTabIndexMutableState.value = index
    }

    fun clearNotifications() {
        notificationsRepository.clear()
        _oldNotifications.value = NotificationsViewState(R.string.generic_error_empty, listOf())
        _newNotifications.value = NotificationsViewState(R.string.generic_error_empty, listOf())
    }

    class NotificationsViewState(@StringRes val textResId: Int, val items: Collection<NotificationMessage>)

}
