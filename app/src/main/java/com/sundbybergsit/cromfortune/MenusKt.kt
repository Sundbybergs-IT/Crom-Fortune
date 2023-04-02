package com.sundbybergsit.cromfortune

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.sundbybergsit.cromfortune.theme.onSurfaceVariant

@Composable
fun OverflowMenu(
    modifier: Modifier = Modifier,
    onNavigateTo: (String) -> Unit,
    contentDescription: String = "Overflow Menu",
    menuColor: Color = MaterialTheme.colors.onSurfaceVariant,
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
