package com.sundbybergsit.cromfortune.main.ui.notifications

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import com.sundbybergsit.cromfortune.domain.notifications.NotificationMessage
import com.sundbybergsit.cromfortune.main.LeafScreen
import com.sundbybergsit.cromfortune.main.OverflowMenu
import com.sundbybergsit.cromfortune.main.PagerStateSelectionHapticFeedbackLaunchedEffect
import com.sundbybergsit.cromfortune.main.R
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun Notifications(
    viewModel: NotificationsViewModel,
    pagerState: PagerState = rememberPagerState(initialPage = 0, pageCount = { 2 }),
    onBack: () -> Unit, onNavigateTo: (String) -> Unit
) {
    val configuration = LocalConfiguration.current
    val formatter = remember(configuration) {
        SimpleDateFormat(
            "yyyy-MM-dd HH:mm", ConfigurationCompat.getLocales(configuration)[0]
        )
    }
    val newNotificationsState by viewModel.newNotifications.collectAsState()
    val oldNotificationsState by viewModel.oldNotifications.collectAsState()
    val tabIndex by viewModel.selectedTabIndexMutableState.collectAsState()

    LaunchedEffect(tabIndex) {
        if (pagerState.currentPage != tabIndex) {
            pagerState.animateScrollToPage(tabIndex)
        }
    }
    LaunchedEffect(pagerState.currentPage) {
        if (pagerState.currentPage != tabIndex) {
            viewModel.selectTab(pagerState.currentPage)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
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
                windowInsets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                ),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back Icon")
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
            LaunchedEffect(pagerState.currentPage) {
                if (pagerState.currentPage != 0) {
                    changedPagerMutableState.value = true
                }
            }
            val view = LocalView.current
            PagerStateSelectionHapticFeedbackLaunchedEffect(
                pagerState = pagerState, view = view, changedState = changedPagerMutableState
            )
            Column {
                PrimaryTabRow(tabIndex) {
                    Tab(
                        text = { Text(text = stringResource(id = R.string.notifications_new_title)) },
                        selected = tabIndex == 0,
                        onClick = {
                            viewModel.selectTab(0)
                        }
                    )
                    Tab(
                        text = { Text(text = stringResource(id = R.string.notifications_old_title)) },
                        selected = tabIndex == 1,
                        onClick = {
                            viewModel.selectTab(1)
                        }
                    )
                }
                HorizontalPager(state = pagerState) { pageIndex ->
                    val currentState = if (pageIndex == 0) newNotificationsState else oldNotificationsState
                    if (currentState.items.isEmpty()) {
                        Text(
                            modifier = Modifier
                                .padding(top = 160.dp)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            text = stringResource(id = R.string.notifications_empty),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            itemsIndexed(currentState.items.toList()) { index, notification ->
                                NotificationsTab(
                                    notification = notification,
                                    backgroundColor = if (index % 2 == 0) {
                                        MaterialTheme.colorScheme.primaryContainer
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
