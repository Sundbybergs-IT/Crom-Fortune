package com.sundbybergsit.cromfortune.ui.notifications

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.os.ConfigurationCompat
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.domain.notifications.NotificationMessage
import com.sundbybergsit.cromfortune.ui.ButtonText
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Notifications(viewModel: NotificationsViewModel, onBack: () -> Unit) {
    val selectedTabIndexMutableState = remember { mutableStateOf(0) }
    val context = LocalContext.current
    val formatter = SimpleDateFormat(
        "yyyy-MM-dd HH:mm", ConfigurationCompat
            .getLocales(context.resources.configuration).get(0)
    )
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
                        viewModel.clearNotifications()
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
                stringResource(id = R.string.notifications_new_title) to viewModel.newNotifications,
                stringResource(id = R.string.notifications_old_title) to viewModel.oldNotifications,
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
                                val newNotification = viewModel.newNotifications.value.elementAt(page)
                                NotificationsTab(
                                    notification = newNotification,
                                    backgroundColor = if (page % 2 == 0) {
                                        MaterialTheme.colors.secondary
                                    } else {
                                        MaterialTheme.colors.background
                                    }, formatter = formatter
                                )
                            }
                            1 -> {
                                val oldNotification = viewModel.oldNotifications.value.elementAt(page)
                                NotificationsTab(
                                    notification = oldNotification,
                                    backgroundColor = if (page % 2 == 0) {
                                        MaterialTheme.colors.secondary
                                    } else {
                                        MaterialTheme.colors.background
                                    }, formatter = formatter
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationsTab(
    notification: NotificationMessage,
    backgroundColor: Color,
    formatter: SimpleDateFormat
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .clickable {
                Toast
                    .makeText(context, R.string.generic_error_not_supported, Toast.LENGTH_LONG)
                    .show()
            }
            .background(color = backgroundColor)
            .padding(8.dp)
            .fillMaxWidth()
    ) {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp), horizontalArrangement = Arrangement.Center
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = formatter.format(Date(notification.dateInMillis)),
                textAlign = TextAlign.Start,
                style = MaterialTheme.typography.body2,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(modifier = Modifier.weight(1f), text = notification.message, style = MaterialTheme.typography.body2)
        }
    }
}
