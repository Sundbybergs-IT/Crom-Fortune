package com.sundbybergsit.cromfortune.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(useDarkColors: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (useDarkColors) DarkColors else LightColors,
        shapes = Shapes(),
        content = content
    )
}

