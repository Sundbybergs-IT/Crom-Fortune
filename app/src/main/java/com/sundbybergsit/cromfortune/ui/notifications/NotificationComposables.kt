package com.sundbybergsit.cromfortune.ui.notifications

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import com.sundbybergsit.cromfortune.R

@Composable
fun Notifications(viewModel: NotificationsViewModel, onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.notifications_title), style = MaterialTheme.typography.h6)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back Icon")
                }
            }
        )
    }) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            // FIXME: https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
        }
    }
}
