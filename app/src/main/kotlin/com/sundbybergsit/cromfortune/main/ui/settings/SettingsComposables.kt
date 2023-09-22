package com.sundbybergsit.cromfortune.main.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ChainStyle
import androidx.constraintlayout.compose.ConstraintLayout
import com.sundbybergsit.cromfortune.main.LeafScreen
import com.sundbybergsit.cromfortune.main.OverflowMenu
import com.sundbybergsit.cromfortune.main.R

@Composable
fun Settings(viewModel: SettingsViewModel, onBack: () -> Unit, onNavigateTo: (String) -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
            title = {
                Text(text = stringResource(id = R.string.settings_title), style = MaterialTheme.typography.titleMedium)
            }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Back Icon")
                }
            }, actions = {
                OverflowMenu(
                    onNavigateTo = onNavigateTo, contentDescription = "Settings Menu",
                    route = LeafScreen.BottomSheetsSettings.route
                )
            })
    }) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            val (titleRef, subtitleRef) = createRefs()
            createVerticalChain(titleRef, subtitleRef, chainStyle = ChainStyle.Packed)
            Column(modifier = Modifier
                .constrainAs(titleRef) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(id = R.string.settings_default_values),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = stringResource(id = R.string.generic_error_not_supported),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            // FIXME: https://github.com/Sundbybergs-IT/Crom-Fortune/issues/21
        }
    }
}
