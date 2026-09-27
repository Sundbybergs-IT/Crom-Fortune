package com.sundbybergsit.cromfortune.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

private const val MENU_SHOW_DELAY_IN_MILLIS = 1500

@Composable
fun OverflowMenu(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "Overflow Menu",
    menuColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    enabled: Boolean = true
) {
    var lastShownMenu by remember { mutableLongStateOf(System.currentTimeMillis()) }

    IconButton(
        modifier = modifier,
        enabled = enabled,
        onClick = {
            if (System.currentTimeMillis() - lastShownMenu > MENU_SHOW_DELAY_IN_MILLIS) {
                lastShownMenu = System.currentTimeMillis()
                onClick()
            }
        }
    ) {
        Icon(
            imageVector = Icons.Outlined.MoreVert,
            tint = if (enabled) menuColor else Color.Gray,
            contentDescription = contentDescription
        )
    }
}
