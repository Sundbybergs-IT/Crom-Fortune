package com.sundbybergsit.cromfortune.ui.notifications

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.domain.notifications.NotificationMessage
import com.sundbybergsit.cromfortune.ui.ButtonText

@Composable
fun Notifications(
    viewModel: NotificationsViewModel, onBack: () -> Unit,
    newNotificationsMutableState: MutableState<Collection<NotificationMessage>> =
        remember {
            mutableStateOf(
                emptyList()
                // FIXME: https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
//                viewModel.newNotifications
            )
        },
    oldNotificationsMutableState: MutableState<Collection<NotificationMessage>> =
        remember { mutableStateOf(
            // FIXME: https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
            emptyList()
            //                viewModel.oldNotifcations
        )
        },
) {
    val context = LocalContext.current
    val selectedTabIndexMutableState = remember { mutableStateOf(0) }
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.notifications_title), style = MaterialTheme.typography.h6)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back Icon")
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        viewModel.clearNotifications(context)
                    }
                ) {
                    ButtonText(
                        text = stringResource(id = R.string.action_clear)
                    )
                }
            }
        )
    }) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            val tabs = listOf(
                stringResource(id = R.string.notifications_new_title) to newNotificationsMutableState,
                stringResource(id = R.string.notifications_old_title) to oldNotificationsMutableState,
            )
            Column {
                TabRow(selectedTabIndexMutableState.value) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            text = { Text(text = title.first) },
                            selected = index == selectedTabIndexMutableState.value,
                            onClick = {
                                selectedTabIndexMutableState.value = index
                            }
                        )
                    }
                }
                LazyColumn {
                    items(tabs[selectedTabIndexMutableState.value].second.value.size) { page ->
                        when (selectedTabIndexMutableState.value) {
                            0 -> {
                                // FIXME: https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
                                // New notifications
                            }
                            1 -> {
                                // FIXME: https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
                                // Old notifications
                            }
                        }
                    }
                }
            }
        }
    }
}
