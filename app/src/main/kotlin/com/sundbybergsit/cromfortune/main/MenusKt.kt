package com.sundbybergsit.cromfortune.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun OverflowMenu(
    modifier: Modifier = Modifier,
    onNavigateTo: (String) -> Unit,
    contentDescription: String = "Overflow Menu",
    menuColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    enabled: Boolean = true,
    route: String
) {
    var showMenu by remember { mutableStateOf(false) }

    IconButton(modifier = modifier, enabled = enabled, onClick = {
        showMenu = !showMenu
        if (showMenu) {
            onNavigateTo.invoke(route)
        }
    }) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            tint = if (enabled) {
                menuColor
            } else {
                Color.Gray
            },
            contentDescription = contentDescription,
        )
    }
}
