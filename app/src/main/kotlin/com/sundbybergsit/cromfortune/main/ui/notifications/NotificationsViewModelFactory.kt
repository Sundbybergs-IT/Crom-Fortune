package com.sundbybergsit.cromfortune.main.ui.notifications

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sundbybergsit.cromfortune.main.notifications.NotificationsRepositoryImpl

class NotificationsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return NotificationsViewModel(notificationsRepository = NotificationsRepositoryImpl(context = context)) as T
    }

}
