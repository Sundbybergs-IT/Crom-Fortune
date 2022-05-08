package com.sundbybergsit.cromfortune.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sundbybergsit.cromfortune.ui.notifications.NotificationsViewModel

class NotificationsViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotificationsViewModel() as T
    }

}
