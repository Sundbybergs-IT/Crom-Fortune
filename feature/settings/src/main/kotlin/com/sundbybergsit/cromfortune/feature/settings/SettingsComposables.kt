package com.sundbybergsit.cromfortune.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun Settings(
    onShowRetrievalIntervals: () -> Unit,
    onShowSupportedStocks: () -> Unit,
    onShowSupportedCryptocurrencies: () -> Unit,
    onShowIssues: () -> Unit,
    onShowAbout: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                windowInsets = WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item {
                SettingsSectionTitle(stringResource(R.string.settings_section_data_updates))
                SettingsRow(
                    title = stringResource(R.string.settings_retrieval_intervals),
                    supportingText = stringResource(R.string.settings_retrieval_intervals_description),
                    onClick = onShowRetrievalIntervals
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
            item {
                SettingsSectionTitle(stringResource(R.string.settings_section_supported_assets))
                SettingsRow(
                    title = stringResource(R.string.settings_supported_stocks),
                    onClick = onShowSupportedStocks
                )
                SettingsRow(
                    title = stringResource(R.string.settings_supported_cryptocurrencies),
                    onClick = onShowSupportedCryptocurrencies
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
            item {
                SettingsSectionTitle(stringResource(R.string.settings_section_application))
                SettingsRow(
                    title = stringResource(R.string.settings_issues),
                    supportingText = stringResource(R.string.settings_issues_description),
                    onClick = onShowIssues
                )
                SettingsRow(
                    title = stringResource(R.string.settings_about),
                    onClick = onShowAbout
                )
            }
        }
    }
}

@Composable
private fun SettingsSectionTitle(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun SettingsRow(
    title: String,
    onClick: () -> Unit,
    supportingText: String? = null
) {
    ListItem(
        headlineContent = {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
        },
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        supportingContent = supportingText?.let { text ->
            {
                Text(
                    text = text,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        trailingContent = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}
