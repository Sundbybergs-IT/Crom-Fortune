package com.sundbybergsit.cromfortune.main.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sundbybergsit.cromfortune.domain.notifications.NotificationMessage
import com.sundbybergsit.cromfortune.domain.notifications.NotificationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class NotificationsViewModel(private val notificationsRepository: NotificationsRepository) : ViewModel() {

    private val _selectedTabIndexMutableState : MutableStateFlow<Int> = MutableStateFlow(0)
    val selectedTabIndexMutableState : StateFlow<Int> = _selectedTabIndexMutableState.asStateFlow()

    private val _newNotifications: MutableStateFlow<NotificationsViewState> = MutableStateFlow(NotificationsViewState())
    val newNotifications: StateFlow<NotificationsViewState> = _newNotifications.asStateFlow()
    private val _oldNotifications: MutableStateFlow<NotificationsViewState> = MutableStateFlow(NotificationsViewState())
    val oldNotifications: StateFlow<NotificationsViewState> = _oldNotifications.asStateFlow()

    init {
        refreshNotifications()
    }

    fun selectTab(index: Int) {
        _selectedTabIndexMutableState.value = index
    }

    fun remove(notificationMessage: NotificationMessage) {
        notificationsRepository.remove(notificationMessage)
        refreshNotifications()
    }

    fun clearNotifications() {
        notificationsRepository.clear()
        _oldNotifications.value = NotificationsViewState()
        _newNotifications.value = NotificationsViewState()
    }

    private fun refreshNotifications() {
        viewModelScope.launch {
            val notifications = notificationsRepository.list().filter { notificationMessage ->
                LocalDate.now().isEqual(
                    Instant.ofEpochMilli(notificationMessage.dateInMillis).atZone(ZoneId.systemDefault()).toLocalDate()
                )
            }.sortedByDescending { notificationMessage -> notificationMessage.dateInMillis }
            _newNotifications.value = NotificationsViewState(notifications)

            val notifications2 = notificationsRepository.list()
                .filter { notificationMessage ->
                    Instant.ofEpochMilli(notificationMessage.dateInMillis).atZone(ZoneId.systemDefault())
                        .toLocalDate().isBefore(LocalDate.now(ZoneId.systemDefault()))
                }.sortedByDescending { notificationMessage -> notificationMessage.dateInMillis }
            _oldNotifications.value = NotificationsViewState(notifications2)
        }
    }

    data class NotificationsViewState(val items: Collection<NotificationMessage> = emptyList())

}
