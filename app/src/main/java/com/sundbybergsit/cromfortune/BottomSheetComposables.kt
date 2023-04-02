package com.sundbybergsit.cromfortune

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BottomSheetContent(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .wrapContentSize()
            .background(color = MaterialTheme.colors.surface)
            .padding(8.dp)
    ) {
        content.invoke()
    }
}

@Composable
fun BottomSheetMenuItem(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    DropdownMenuItem(
        modifier = Modifier.height(48.dp),
        onClick = onClick,
        enabled = enabled,
        text = {
            Text(
                text = text,
                style = MaterialTheme.typography.body1,
                color = if (enabled) MaterialTheme.colors.onSurface else MaterialTheme.colors.onSurface.disabled()
            )
        }
    )
}

private fun Color.disabled(): Color = this.copy(alpha = 0.38f)
