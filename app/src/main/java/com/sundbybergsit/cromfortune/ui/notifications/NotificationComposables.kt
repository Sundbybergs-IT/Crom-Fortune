package com.sundbybergsit.cromfortune.ui.notifications

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import com.sundbybergsit.cromfortune.LeafScreen
import com.sundbybergsit.cromfortune.OverflowMenu
import com.sundbybergsit.cromfortune.PagerStateSelectionHapticFeedbackLaunchedEffect
import com.sundbybergsit.cromfortune.R
import com.sundbybergsit.cromfortune.domain.notifications.NotificationMessage
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun Notifications(viewModel: NotificationsViewModel,
                  pagerState: PagerState = rememberPagerState(initialPage = 0, pageCount = { 2 }),
                  onBack: () -> Unit, onNavigateTo: (String) -> Unit) {
    val context = LocalContext.current
    val formatter = SimpleDateFormat(
        "yyyy-MM-dd HH:mm", ConfigurationCompat.getLocales(context.resources.configuration)[0]
    )
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(id = R.string.notifications_title),
                    style = MaterialTheme.typography.titleMedium
                )
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back Icon")
                }
            },
            actions = {
                OverflowMenu(
                    onNavigateTo = onNavigateTo, contentDescription = "Notifications Menu",
                    route = LeafScreen.BottomSheetsNotifications.route
                )
            }
        )
    }) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues = paddingValues)) {
            val changedPagerMutableState = remember { mutableStateOf(false) }
            val view = LocalView.current
            PagerStateSelectionHapticFeedbackLaunchedEffect(
                pagerState = pagerState, view = view, changedState = changedPagerMutableState
            )
            val tabs = listOf(
                stringResource(id = R.string.notifications_new_title) to viewModel.newNotifications,
                stringResource(id = R.string.notifications_old_title) to viewModel.oldNotifications,
            )
            Column {
                TabRow(viewModel.selectedTabIndexMutableState.value) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            text = { Text(text = title.first) },
                            selected = index == viewModel.selectedTabIndexMutableState.value,
                            onClick = {
                                viewModel.selectTab(index)
                            }
                        )
                    }
                }
                LazyColumn {
                    items(tabs[viewModel.selectedTabIndexMutableState.value].second.value.items.size) { page ->
                        when (viewModel.selectedTabIndexMutableState.value) {
                            0 -> {
                                val newNotification = viewModel.newNotifications.value.items.elementAt(page)
                                NotificationsTab(
                                    notification = newNotification,
                                    backgroundColor = if (page % 2 == 0) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        MaterialTheme.colorScheme.background
                                    }, formatter = formatter
                                )
                            }

                            1 -> {
                                val oldNotification = viewModel.oldNotifications.value.items.elementAt(page)
                                NotificationsTab(
                                    notification = oldNotification,
                                    backgroundColor = if (page % 2 == 0) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        MaterialTheme.colorScheme.background
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
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                modifier = Modifier.weight(1f),
                text = notification.message,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
